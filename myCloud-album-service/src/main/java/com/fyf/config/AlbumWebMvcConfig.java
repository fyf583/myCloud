package com.fyf.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 把照片落盘目录映射成静态资源：GET /album/file/{存储文件名}
 * <p>
 * 注意：该路径需要在网关 AuthGlobalFilter 的 EXCLUDE_PATH 中放行，
 * 因为浏览器 &lt;img&gt; 标签无法携带 token 请求头。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AlbumWebMvcConfig implements WebMvcConfigurer {

    private final AlbumStorageProperties properties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String urlPrefix = properties.getUrlPrefix();
        while (urlPrefix.endsWith("/")) {
            urlPrefix = urlPrefix.substring(0, urlPrefix.length() - 1);
        }

        Path dir = Paths.get(properties.getStoragePath()).toAbsolutePath().normalize();
        String location = dir.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler(urlPrefix + "/**").addResourceLocations(location);
        log.info("相册静态资源映射：{}/** -> {}", urlPrefix, location);
    }
}
