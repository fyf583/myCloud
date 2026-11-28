package com.fyf.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件返回给前端的视图对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileDTO {
    private Long id;

    /** 展示用文件名 */
    private String fileName;

    private String description;

    /** 文件大小（字节） */
    private Long size;

    /** 下载地址（相对网关：/file/download/xxx.ext） */
    private String url;

    private LocalDateTime createTime;

    /** 磁盘文件名：只在服务内部用于拼 URL / 定位文件，不返回给前端 */
    @JsonIgnore
    private String storedName;
}
