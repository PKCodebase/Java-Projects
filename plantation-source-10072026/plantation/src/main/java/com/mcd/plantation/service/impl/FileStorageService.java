package com.mcd.plantation.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

/**
 * Stores uploaded files on the local filesystem and returns a publicly
 * accessible URL (served by WebMvcConfig's resource handler at /uploads/**).
 */
@Service
@Slf4j
public class FileStorageService {

    @Value("${storage.upload-dir}")
    private String uploadDir;

    @Value("${storage.base-url}")
    private String baseUrl;

    /**
     * Persist a multipart upload under &lt;uploadDir&gt;/&lt;subfolder&gt;/
     *
     * @param file      the incoming file
     * @param subfolder e.g. "plantation" or "certificates"
     * @return full public URL for the stored file
     */
    public String store(MultipartFile file, String subfolder) {
        String rawName = file.getOriginalFilename();
        String original = StringUtils.cleanPath(rawName != null ? rawName : "upload");
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + ext;
        return writeBytes(subfolder, filename, readBytes(file));
    }

    /**
     * Persist raw bytes (e.g. a generated PDF).
     *
     * @param data      byte array to write
     * @param subfolder e.g. "certificates"
     * @param filename  target filename (will be used as-is)
     * @return full public URL for the stored file
     */
    public String storeBytes(byte[] data, String subfolder, String filename) {
        return writeBytes(subfolder, filename, data);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException("Could not read upload bytes: " + e.getMessage(), e);
        }
    }

    private String writeBytes(String subfolder, String filename, byte[] data) {
        try {
            Path dir = Paths.get(uploadDir, subfolder);
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.write(target, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Stored file: {}", target.toAbsolutePath());
            return baseUrl + "/" + subfolder + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not write file: " + e.getMessage(), e);
        }
    }
}
