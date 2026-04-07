package com.hdisla3tak.app.service;

import com.hdisla3tak.app.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Path FALLBACK_UPLOAD_ROOT = Paths.get(System.getProperty("java.io.tmpdir"), "hdi-sla3tak", "uploads", "items")
        .toAbsolutePath()
        .normalize();
    private final Path configuredUploadRoot;
    private final Path uploadRoot;
    private final boolean usingFallbackStorage;

    public FileStorageService(AppProperties properties) {
        this.configuredUploadRoot = Paths.get(properties.getStorage().getUploadDir()).toAbsolutePath().normalize();
        UploadRootSelection selection = initializeUploadRoot(configuredUploadRoot);
        this.uploadRoot = selection.path();
        this.usingFallbackStorage = selection.fallback();
        log.info("Upload storage configured path: {}, active path: {}, using fallback storage: {}",
            configuredUploadRoot, uploadRoot, usingFallbackStorage);
    }

    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPG, PNG, WEBP, or GIF images are allowed.");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be smaller than 5 MB.");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeName = UUID.randomUUID() + (extension != null ? "." + extension.toLowerCase() : "");
        Path target = uploadRoot.resolve(safeName);
        try {
            Files.createDirectories(uploadRoot);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            log.info("Saved uploaded image to {} (success=true)", target.toAbsolutePath().normalize());
        } catch (IOException e) {
            log.error("Failed to save uploaded image to {} (success=false)", target.toAbsolutePath().normalize(), e);
            throw new IllegalStateException("Could not save image.", e);
        }
        return safeName;
    }

    private UploadRootSelection initializeUploadRoot(Path configuredRoot) {
        if (ensureWritableDirectory(configuredRoot)) {
            return new UploadRootSelection(configuredRoot, false);
        }
        if (ensureWritableDirectory(FALLBACK_UPLOAD_ROOT)) {
            log.warn("Using fallback upload directory {} because configured directory {} is not writable.", FALLBACK_UPLOAD_ROOT, configuredRoot);
            return new UploadRootSelection(FALLBACK_UPLOAD_ROOT, true);
        }
        throw new IllegalStateException("Could not initialize an upload directory.");
    }

    private boolean ensureWritableDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private record UploadRootSelection(Path path, boolean fallback) {
    }
}
