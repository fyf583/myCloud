package com.fyf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传照片的返回结果（对应 myCloud.md 5.4：data.url）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumUploadDTO {
    private String url;
}
