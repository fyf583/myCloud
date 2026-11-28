package com.fyf.service.impl;

import com.fyf.config.AlbumStorageProperties;
import com.fyf.dto.AlbumDTO;
import com.fyf.dto.AlbumUploadDTO;
import com.fyf.dto.CursorPage;
import com.fyf.entity.Album;
import com.fyf.enums.AlbumStatusEnum;
import com.fyf.exception.AlbumException;
import com.fyf.exception.LoginException;
import com.fyf.mapper.AlbumMapper;
import com.fyf.result.CursorPageResult;
import com.fyf.service.AlbumService;
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
public class AlbumServiceImpl implements AlbumService {

    /** 每页最大条数 */
    private static final int MAX_PAGE_SIZE = 20;

    /** 照片名称最大长度（与 album.photo_name 一致） */
    private static final int MAX_PHOTO_NAME_LENGTH = 100;

    /** 照片描述最大长度（与 album.description 一致） */
    private static final int MAX_DESCRIPTION_LENGTH = 255;

    @Resource
    private AlbumMapper albumMapper;

    @Resource
    private AlbumStorageProperties storageProperties;

    /** 启动时确保存储目录存在，方便排查“上传失败”类问题 */
    @PostConstruct
    public void initStorageDir() {
        try {
            log.info("相册照片存储目录：{}", storageDir());
        } catch (AlbumException e) {
            log.error("相册照片存储目录初始化失败：{}", e.getMessage());
        }
    }

    /**
     * 相册列表：游标分页（create_time desc, id desc）
     */
    @Override
    public CursorPageResult<AlbumDTO> albumList(CursorPage cursorPage) {
        Long userId = currentUserId();
        CursorPage page = cursorPage == null ? new CursorPage() : cursorPage;
        int pageSize = resolvePageSize(page);

        List<AlbumDTO> list = albumMapper.getAlbums(pageSize + 1, page.getCursorId(), page.getCursorTime(),
                userId, AlbumStatusEnum.NORMAL.getCode());

        boolean hasMore = list.size() > pageSize;
        if (hasMore) {
            list.remove(list.size() - 1);
        }

        CursorPageResult<AlbumDTO> result = new CursorPageResult<>();
        result.setHasMore(hasMore);
        result.setList(list);
        if (!list.isEmpty()) {
            AlbumDTO last = list.get(list.size() - 1);
            result.setNextCursorId(last.getId());
            result.setNextCursorTime(last.getCreateTime());
        }
        return result;
    }

    /**
     * 查看照片（只能看自己的）
     */
    @Override
    public AlbumDTO getPhoto(Long photoId) {
        Long userId = currentUserId();
        AlbumDTO photo = albumMapper.getAlbumById(photoId, userId, AlbumStatusEnum.NORMAL.getCode());
        if (photo == null) {
            throw new AlbumException("照片不存在");
        }
        return photo;
    }

    /**
     * 上传照片：校验 -> 落盘（UUID 文件名）-> 写库
     */
    @Override
    public AlbumUploadDTO uploadPhoto(MultipartFile file, String photoName, String description) {
        Long userId = currentUserId();
        if (file == null || file.isEmpty()) {
            throw new AlbumException("请选择要上传的照片");
        }
        if (file.getSize() > storageProperties.getMaxFileSize()) {
            throw new AlbumException("照片大小不能超过 " + (storageProperties.getMaxFileSize() / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new AlbumException("只支持上传图片文件");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        // 用 UUID 做磁盘文件名：避免同名覆盖，也避免中文/特殊字符带来的路径问题
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = storageDir().resolve(storedName);
        try {
            file.transferTo(target);
        } catch (IOException | IllegalStateException e) {
            log.error("照片落盘失败：{}", target, e);
            throw new AlbumException("照片保存失败，请稍后重试");
        }

        Album album = new Album();
        album.setUserId(userId);
        album.setPhotoName(resolvePhotoName(photoName, file.getOriginalFilename()));
        album.setDescription(resolveDescription(description));
        album.setUrl(buildUrl(storedName));
        album.setStatus(AlbumStatusEnum.NORMAL.getCode());

        try {
            if (albumMapper.insertAlbum(album) != 1) {
                throw new AlbumException("照片上传失败");
            }
        } catch (RuntimeException e) {
            // 落库失败时删掉刚写入磁盘的文件，避免产生孤儿文件
            deleteQuietly(target);
            throw e;
        }

        log.info("用户 {} 上传照片 {}（{} 字节）", userId, storedName, file.getSize());
        return new AlbumUploadDTO(album.getUrl());
    }

    /**
     * 删除照片：先软删除数据库记录，再删除磁盘文件（文件删不掉只记录日志，不影响接口结果）
     */
    @Override
    public void deletePhoto(Long photoId) {
        Long userId = currentUserId();
        AlbumDTO photo = albumMapper.getAlbumById(photoId, userId, AlbumStatusEnum.NORMAL.getCode());
        if (photo == null) {
            throw new AlbumException("照片不存在");
        }

        Album album = new Album();
        album.setId(photoId);
        album.setStatus(AlbumStatusEnum.DELETE.getCode());
        if (albumMapper.updateAlbum(album, userId) != 1) {
            throw new AlbumException("删除照片失败");
        }

        deleteStoredFile(photo.getUrl());
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

    /** 照片存储目录（不存在时自动创建） */
    private Path storageDir() {
        Path dir = Paths.get(storageProperties.getStoragePath()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.error("照片存储目录创建失败：{}", dir, e);
            throw new AlbumException("照片存储目录不可用：" + dir);
        }
        return dir;
    }

    /** 取文件扩展名并校验白名单 */
    private String resolveExtension(String originalFilename) {
        String name = originalFilename == null ? "" : originalFilename;
        int dotIndex = name.lastIndexOf('.');
        String extension = dotIndex >= 0 ? name.substring(dotIndex + 1).toLowerCase(Locale.ROOT) : "";
        if (!storageProperties.getAllowedExtensions().contains(extension)) {
            throw new AlbumException("不支持的图片格式，仅支持：" +
                    String.join("/", storageProperties.getAllowedExtensions()));
        }
        return extension;
    }

    /** 照片名称：优先用前端传的，其次用原始文件名，超长截断 */
    private String resolvePhotoName(String photoName, String originalFilename) {
        String name = photoName == null ? "" : photoName.trim();
        if (name.isEmpty() && originalFilename != null) {
            name = originalFilename.trim();
        }
        if (name.isEmpty()) {
            name = "未命名照片";
        }
        return name.length() > MAX_PHOTO_NAME_LENGTH ? name.substring(0, MAX_PHOTO_NAME_LENGTH) : name;
    }

    /** 描述为空存 null，超长直接报错提示 */
    private String resolveDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            throw new AlbumException("照片描述不能超过 " + MAX_DESCRIPTION_LENGTH + " 个字符");
        }
        return trimmed;
    }

    /** 相对访问地址：/album/file/{UUID}.{ext} */
    private String buildUrl(String storedName) {
        String prefix = storageProperties.getUrlPrefix();
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + "/" + storedName;
    }

    /** 根据数据库中的 url 删除磁盘文件（防目录穿越） */
    private void deleteStoredFile(String url) {
        if (url == null || url.isBlank()) {
            return;
        }
        String prefix = storageProperties.getUrlPrefix();
        while (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        if (!url.startsWith(prefix + "/")) {
            log.warn("照片地址不在相册存储前缀下，跳过删除：{}", url);
            return;
        }

        String fileName = url.substring(prefix.length() + 1);
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            log.warn("非法的照片文件名，跳过删除：{}", fileName);
            return;
        }

        Path dir = Paths.get(storageProperties.getStoragePath()).toAbsolutePath().normalize();
        Path target = dir.resolve(fileName).normalize();
        if (!target.startsWith(dir)) {
            log.warn("照片路径越界，跳过删除：{}", target);
            return;
        }
        deleteQuietly(target);
    }

    private void deleteQuietly(Path path) {
        try {
            if (Files.deleteIfExists(path)) {
                log.info("已删除照片文件：{}", path);
            }
        } catch (IOException e) {
            log.warn("删除照片文件失败：{}", path, e);
        }
    }
}
