package com.fyf.service;

import com.fyf.dto.CursorPage;
import com.fyf.dto.FileDTO;
import com.fyf.dto.FileUploadDTO;
import com.fyf.result.CursorPageResult;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileService {

    /** 文件列表（游标分页） */
    CursorPageResult<FileDTO> fileList(CursorPage cursorPage);

    /** 上传文件：落盘 + 写库 */
    FileUploadDTO uploadFile(MultipartFile multipartFile, String fileName, String description);

    /** 获取下载信息（需要登录，只能取自己的文件） */
    FileDTO getDownloadInfo(Long fileId);

    /** 按磁盘文件名取文件信息（下载接口用，无需登录态） */
    FileDTO getByStoredName(String storedName);

    /** 解析磁盘路径（含目录穿越校验） */
    Path resolveStoredPath(String storedName);

    /** 删除文件（软删除 + 删除磁盘文件） */
    void deleteFile(Long fileId);
}
