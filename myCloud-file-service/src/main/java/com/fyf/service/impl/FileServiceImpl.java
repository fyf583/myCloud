package com.fyf.service.impl;

import com.fyf.config.FileStorageProperties;
import com.fyf.dto.CursorPage;
import com.fyf.dto.FileDTO;
import com.fyf.dto.FileUploadDTO;
import com.fyf.entity.FileInfo;
import com.fyf.enums.FileStatusEnum;
import com.fyf.exception.FileException;
import com.fyf.exception.LoginException;
import com.fyf.mapper.FileInfoMapper;
import com.fyf.result.CursorPageResult;
import com.fyf.service.FileService;
import com.fyf.util.UserContext;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class FileServiceImpl implements FileService {

    /** 每页最大条数 */
    private static final int MAX_PAGE_SIZE = 20;

    /** 文件名最大长度（与 file.file_name 一致） */
    private static final int MAX_NAME_LENGTH = 150;

    /** 描述最大长度（与 file.description 一致） */
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    @Resource
    private FileInfoMapper fileInfoMapper;

    @Resource
    private FileStorageProperties storageProperties;

    /** 启动时确保存储目录存在 */
    @PostConstruct
    public void initStorageDir() {
        try {
            log.info("文件存储目录：{}", storageDir());
        } catch (FileException e) {
            log.error("文件存储目录初始化失败：{}", e.getMessage());
        }
    }

    /**
     * 文件列表：游标分页（create_time desc, id desc）
     */
    @Override
    public CursorPageResult<FileDTO> fileList(CursorPage cursorPage) {
        Long userId = currentUserId();
        CursorPage page = cursorPage == null ? new CursorPage() : cursorPage;
        int pageSize = resolvePageSize(page);

        List<FileDTO> list = fileInfoMapper.getFiles(pageSize + 1, page.getCursorId(), page.getCursorTime(),
                userId, FileStatusEnum.NORMAL.getCode());
        list.forEach(file -> file.setUrl(buildDownloadUrl(file.getStoredName())));

        boolean hasMore = list.size() > pageSize;
        if (hasMore) {
            list.remove(list.size() - 1);
        }

        CursorPageResult<FileDTO> result = new CursorPageResult<>();
        result.setHasMore(hasMore);
        result.setList(list);
        if (!list.isEmpty()) {
            FileDTO last = list.get(list.size() - 1);
            result.setNextCursorId(last.getId());
            result.setNextCursorTime(last.getCreateTime());
        }
        return result;
    }

    /**
     * 上传文件：校验 -> 落盘（UUID 文件名）-> 写库
     */
    @Override
    public FileUploadDTO uploadFile(MultipartFile multipartFile, String fileName, String description) {
        Long userId = currentUserId();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileException("请选择要上传的文件");
        }
        if (multipartFile.getSize() > storageProperties.getMaxFileSize()) {
            throw new FileException("文件大小不能超过 " + (storageProperties.getMaxFileSize() / 1024 / 1024) + "MB");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        String extension = resolveExtension(originalFilename);
        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (extension.isEmpty() ? "" : "." + extension);
        Path target = storageDir().resolve(storedName);
        try {
            multipartFile.transferTo(target);
        } catch (IOException | IllegalStateException e) {
            log.error("文件落盘失败：{}", target, e);
            throw new FileException("文件保存失败，请稍后重试");
        }

        FileInfo file = new FileInfo();
        file.setUserId(userId);
        file.setFileName(resolveFileName(fileName, originalFilename));
        file.setDescription(resolveDescription(description));
        file.setSize(multipartFile.getSize());
        file.setStoredName(storedName);
        file.setStatus(FileStatusEnum.NORMAL.getCode());

        try {
            if (fileInfoMapper.insertFile(file) != 1) {
                throw new FileException("文件上传失败");
            }
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }

        log.info("用户 {} 上传文件 {}（{} 字节）", userId, storedName, multipartFile.getSize());
        return new FileUploadDTO(file.getId(), buildDownloadUrl(storedName));
    }

    /**
     * 获取下载信息（需要登录，只能取自己的文件）
     */
    @Override
    public FileDTO getDownloadInfo(Long fileId) {
        Long userId = currentUserId();
        FileDTO file = fileInfoMapper.getFileById(fileId, userId, FileStatusEnum.NORMAL.getCode());
        if (file == null) {
            throw new FileException("文件不存在或已删除");
        }
        file.setUrl(buildDownloadUrl(file.getStoredName()));
        return file;
    }

    /**
     * 按磁盘文件名取文件信息（下载接口用；文件名是 UUID，不可枚举）
     */
    @Override
    public FileDTO getByStoredName(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            throw new FileException("文件不存在或已删除");
        }
        FileDTO file = fileInfoMapper.getFileByStoredName(storedName, FileStatusEnum.NORMAL.getCode());
        if (file == null) {
            throw new FileException("文件不存在或已删除");
        }
        file.setUrl(buildDownloadUrl(file.getStoredName()));
        return file;
    }

    /**
     * 解析磁盘路径（含目录穿越校验）
     */
    @Override
    public Path resolveStoredPath(String storedName) {
        if (storedName == null || storedName.isBlank()
                || storedName.contains("/") || storedName.contains("\\") || storedName.contains("..")) {
            throw new FileException("非法的文件名");
        }
        Path dir = Paths.get(storageProperties.getStoragePath()).toAbsolutePath().normalize();
        Path target = dir.resolve(storedName).normalize();
        if (!target.startsWith(dir)) {
            throw new FileException("非法的文件路径");
        }
        if (!Files.isRegularFile(target)) {
            throw new FileException("文件不存在或已删除");
        }
        return target;
    }

    /**
     * 删除文件：先软删除数据库记录，再删除磁盘文件
     */
    @Override
    public void deleteFile(Long fileId) {
        Long userId = currentUserId();
        FileDTO existing = fileInfoMapper.getFileById(fileId, userId, FileStatusEnum.NORMAL.getCode());
        if (existing == null) {
            throw new FileException("文件不存在或已删除");
        }

        FileInfo file = new FileInfo();
        file.setId(fileId);
        file.setStatus(FileStatusEnum.DELETE.getCode());
        if (fileInfoMapper.updateFile(file, userId) != 1) {
            throw new FileException("删除文件失败");
        }

        try {
            deleteQuietly(resolveStoredPath(existing.getStoredName()));
        } catch (FileException e) {
            log.warn("磁盘文件不存在，跳过删除：{}", existing.getStoredName());
        }
    }

    /** 取当前登录用户（由网关 X-User-Id 透传），未登录抛异常 */
    private Long currentUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new LoginException("请先登录");
        }
        return userId;
    }

    /** pageSize 兜底：空/非正数/超上限都按 20 处理 */
    private int resolvePageSize(CursorPage page) {
        Integer pageSize = page.getPageSize();
        if (pageSize == null || pageSize <= 0 || pageSize > MAX_PAGE_SIZE) {
            return MAX_PAGE_SIZE;
        }
        return pageSize;
    }

    /** 文件存储目录（不存在时自动创建） */
    private Path storageDir() {
        Path dir = Paths.get(storageProperties.getStoragePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("文件存储目录创建失败：{}", dir, e);
            throw new FileException("文件存储目录不可用：" + dir);
        }
        return dir;
    }

    /** 取扩展名（只保留字母数字，最长 10 位；没有扩展名返回空串） */
    private String resolveExtension(String originalFilename) {
        String name = originalFilename == null ? "" : originalFilename;
        int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "";
        }
        String extension = name.substring(dotIndex + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return extension.length() > 10 ? extension.substring(0, 10) : extension;
    }

    /** 展示文件名：优先用前端传的，其次用原始文件名，超长截断 */
    private String resolveFileName(String fileName, String originalFilename) {
        String name = fileName == null ? "" : fileName.trim();
        if (name.isEmpty() && originalFilename != null) {
            name = originalFilename.trim();
        }
        if (name.isEmpty()) {
            name = "未命名文件";
        }
        return name.length() > MAX_NAME_LENGTH ? name.substring(0, MAX_NAME_LENGTH) : name;
    }

    /** 描述为空存 null，超长直接报错 */
    private String resolveDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new FileException("文件描述不能超过 " + MAX_DESCRIPTION_LENGTH + " 个字符");
        }
        return trimmed;
    }

    /** 相对下载地址：/file/download/{UUID}.ext */
    private String buildDownloadUrl(String storedName) {
        String prefix = storageProperties.getDownloadPrefix();
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/" + storedName;
    }

    private void deleteQuietly(Path path) {
        try {
            if (path != null && Files.deleteIfExists(path)) {
                log.info("已删除文件：{}", path);
            }
        } catch (IOException e) {
            log.warn("删除文件失败：{}", path, e);
        }
    }
}
