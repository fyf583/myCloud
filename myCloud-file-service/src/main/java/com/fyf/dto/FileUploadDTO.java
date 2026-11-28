package com.fyf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传文件的返回结果（对应 myCloud.md 7.3：data.fileId + data.url）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadDTO {
    private Long fileId;

    private String url;
}
