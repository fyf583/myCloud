package com.fyf.controller;

import com.fyf.dto.AlbumDTO;
import com.fyf.dto.AlbumUploadDTO;
import com.fyf.dto.CursorPage;
import com.fyf.result.CursorPageResult;
import com.fyf.result.Result;
import com.fyf.service.AlbumService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 相册模块（对应 myCloud.md 第 5 章）
 * <pre>
 * GET  /album                 相册列表（游标分页）
 * GET  /album/{photoId}       查看照片
 * POST /album                 上传照片（multipart/form-data）
 * POST /album/rm/{photoId}    删除照片
 * GET  /album/file/{fileName} 图片静态访问（由 AlbumWebMvcConfig 映射，网关照开放行）
 * </pre>
 */
@RestController
@Slf4j
@RequestMapping("/album")
public class AlbumController {

    @Resource
    private AlbumService albumService;

    /** 相册列表（游标分页） */
    @GetMapping
    public Result<CursorPageResult<AlbumDTO>> albumList(CursorPage cursorPage) {
        return Result.success(albumService.albumList(cursorPage));
    }

    /** 查看照片 */
    @GetMapping("/{photoId}")
    public Result<AlbumDTO> getPhoto(@PathVariable("photoId") Long photoId) {
        return Result.success(albumService.getPhoto(photoId));
    }

    /** 上传照片（multipart/form-data：file / photoName / description） */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AlbumUploadDTO> uploadPhoto(@RequestParam("file") MultipartFile file,
                                              @RequestParam(value = "photoName", required = false) String photoName,
                                              @RequestParam(value = "description", required = false) String description) {
        return Result.success(albumService.uploadPhoto(file, photoName, description));
    }

    /** 删除照片（软删除 + 删除磁盘文件） */
    @PostMapping("/rm/{photoId}")
    public Result<Void> deletePhoto(@PathVariable("photoId") Long photoId) {
        albumService.deletePhoto(photoId);
        return Result.success();
    }
}
