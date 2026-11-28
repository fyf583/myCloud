package com.fyf.controller;

import com.fyf.dto.CursorPage;
import com.fyf.dto.FileDTO;
import com.fyf.dto.FileUploadDTO;
import com.fyf.exception.FileException;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * 文件分享模块（对应 myCloud.md 第 7 章）
 * <pre>
 * GET  /file                      文件列表（游标分页）
 * POST /file                      上传文件（multipart/form-data）
 * GET  /file/{fileId}             获取下载信息（JSON）
 * GET  /file/download/{storedName} 实际下载（文件流，网关放行、无需 token）
 * POST /file/rm/{fileId}          删除文件
 * </pre>
 */
@RestController
@Slf4j
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    /** 文件列表（游标分页） */
    @GetMapping
    public Result<CursorPageResult<FileDTO>> fileList(CursorPage cursorPage) {
        return Result.success(fileService.fileList(cursorPage));
    }

    /** 上传文件（multipart/form-data：file / fileName / description） */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<FileUploadDTO> uploadFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam(value = "fileName", required = false) String fileName,
                                            @RequestParam(value = "description", required = false) String description) {
        return Result.success(fileService.uploadFile(file, fileName, description));
    }

    /** 获取下载信息（前端拿到 url 后再去 /file/download/xxx 下载） */
    @GetMapping("/{fileId}")
    public Result<FileDTO> downloadInfo(@PathVariable("fileId") Long fileId) {
        return Result.success(fileService.getDownloadInfo(fileId));
    }

    /** 实际下载：返回文件流，并带上原始文件名 */
    @GetMapping("/download/{storedName:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable("storedName") String storedName) {
        try {
            FileDTO info = fileService.getByStoredName(storedName);
            Path path = fileService.resolveStoredPath(storedName);
            String encodedName = URLEncoder.encode(info.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(path.toFile().length())
                    .body(new FileSystemResource(path));
        } catch (FileException e) {
            log.warn("文件下载失败：{}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /** 删除文件（软删除 + 删除磁盘文件） */
    @PostMapping("/rm/{fileId}")
    public Result<Void> deleteFile(@PathVariable("fileId") Long fileId) {
        fileService.deleteFile(fileId);
        return Result.success();
    }
}
