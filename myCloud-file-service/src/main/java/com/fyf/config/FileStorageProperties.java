package com.fyf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储配置（application.yaml 中的 mycloud.file.*）
 */
@Data
@Component
@ConfigurationProperties(prefix = "mycloud.file")
public class FileStorageProperties {

    /** 文件落盘目录，默认用户目录下；Windows 可改成 D:/myCloud/uploads/file */
    private String storagePath = System.getProperty("user.home") + "/myCloud/uploads/file";

    /** 下载地址前缀（需在网关 AuthGlobalFilter 中放行） */
    private String downloadPrefix = "/file/download";

    /** 单个文件大小上限（字节），默认 50MB */
    private long maxFileSize = 50 * 1024 * 1024;
}
