package com.fyf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 相册照片返回给前端的视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlbumDTO {
    private Long id;

    /** 照片名称 */
    private String photoName;

    /** 照片描述 */
    private String description;

    /** 照片地址（相对网关的路径，前端拼上 BASE_URL 使用） */
    private String url;

    private LocalDateTime createTime;
}
