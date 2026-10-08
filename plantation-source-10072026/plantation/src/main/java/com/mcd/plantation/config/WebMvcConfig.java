package com.mcd.plantation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves locally-stored plantation photos and certificate PDFs
 * at  /uploads/**  →  <storage.upload-dir>/**
 *
 * Full URL example: http://localhost:8080/plantation/uploads/plantation/abc.jpg
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${storage.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Normalise Windows backslashes and ensure trailing slash
        String location = "file:///" + uploadDir.replace("\\", "/");
        if (!location.endsWith("/")) location += "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
