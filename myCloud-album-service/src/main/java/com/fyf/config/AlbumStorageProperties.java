package com.fyf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 相册照片存储配置（application.yaml 中的 mycloud.album.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "mycloud.album")
public class AlbumStorageProperties {

    /** 照片落盘目录，默认用户目录下；Windows 可改成 D:/myCloud/uploads/album */
    private String storagePath = System.getProperty("user.home") + "/myCloud/uploads/album";

    /** 对外访问前缀（必须与网关放行路径、前端拼接方式保持一致） */
    private String urlPrefix = "/album/file";

    /** 允许的图片扩展名（小写） */
    private List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "webp", "bmp");

    /** 单张照片大小上限（字节），默认 10MB，与 spring.servlet.multipart.max-file-size 保持一致 */
    private long maxFileSize = 10 * 1024 * 1024;
}
