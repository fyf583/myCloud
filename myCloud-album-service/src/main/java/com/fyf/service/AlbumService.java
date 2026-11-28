package com.fyf.service;

import com.fyf.dto.AlbumDTO;
import com.fyf.dto.AlbumUploadDTO;
import com.fyf.dto.CursorPage;
import com.fyf.result.CursorPageResult;
import org.springframework.web.multipart.MultipartFile;

public interface AlbumService {

    /** 相册列表（游标分页） */
    CursorPageResult<AlbumDTO> albumList(CursorPage cursorPage);

    /** 查看照片 */
    AlbumDTO getPhoto(Long photoId);

    /** 上传照片：文件落盘 + 写库，返回可访问地址 */
    AlbumUploadDTO uploadPhoto(MultipartFile file, String photoName, String description);

    /** 删除照片（软删除 + 删除磁盘文件） */
    void deletePhoto(Long photoId);
}
