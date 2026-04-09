package com.hdisla3tak.app.service;

import com.hdisla3tak.app.config.AppProperties;
import com.hdisla3tak.app.domain.Shop;
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
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Path FALLBACK_UPLOAD_ROOT = Paths.get(System.getProperty("java.io.tmpdir"), "hdi-sla3tak", "uploads", "items")
        .toAbsolutePath()
        .normalize();
    private static final String ITEM_DIRECTORY_NAME = "items";
    private final Path configuredUploadRoot;
    private final Path uploadRoot;
    private final Path itemUploadRoot;
    private final boolean usingFallbackStorage;

    public FileStorageService(AppProperties properties) {
        this.configuredUploadRoot = Paths.get(properties.getStorage().getUploadDir()).toAbsolutePath().normalize();
        logPathState("Configured upload path before initialization", configuredUploadRoot);
        UploadRootSelection selection = initializeUploadRoot(configuredUploadRoot);
        this.uploadRoot = selection.path();
        this.usingFallbackStorage = selection.fallback();
        this.itemUploadRoot = resolveItemUploadRoot(uploadRoot, configuredUploadRoot);
        ensureWritableDirectory(itemUploadRoot);
        logPathState("Configured upload path after initialization", configuredUploadRoot);
        logPathState("Fallback upload path", FALLBACK_UPLOAD_ROOT);
        logPathState("Active upload path", uploadRoot);
        logPathState("Tenant item upload path", itemUploadRoot);
        log.info("Upload storage configured path: {}, active path: {}, tenant item path: {}, using fallback storage: {}",
            configuredUploadRoot, uploadRoot, itemUploadRoot, usingFallbackStorage);
    }

    public String storeImage(MultipartFile file) {
        return storeImage(file, null);
    }

    public String storeImage(MultipartFile file, Shop shop) {
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
        Path tenantDirectory = resolveTenantUploadDirectory(shop);
        Path target = tenantDirectory.resolve(safeName);
        boolean directoryExistsBeforeSave = Files.exists(tenantDirectory);
        boolean directoryIsDirectoryBeforeSave = Files.isDirectory(tenantDirectory);
        boolean directoryIsWritableBeforeSave = Files.isWritable(tenantDirectory);
        String storedPath = normalizeStoredPath(itemUploadRoot.relativize(target));
        log.info(
            "Image upload attempt: originalFilename={}, storedFilename={}, activeUploadDir={}, tenantUploadDir={}, targetPath={}, dirExistsBeforeSave={}, dirIsDirectoryBeforeSave={}, dirIsWritableBeforeSave={}",
            file.getOriginalFilename(),
            storedPath,
            itemUploadRoot,
            tenantDirectory,
            target.toAbsolutePath().normalize(),
            directoryExistsBeforeSave,
            directoryIsDirectoryBeforeSave,
            directoryIsWritableBeforeSave
        );
        try {
            Files.createDirectories(tenantDirectory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            boolean fileExistsAfterSave = Files.exists(target);
            Long fileSizeAfterSaveBytes = fileExistsAfterSave ? Files.size(target) : null;
            log.info(
                "Image upload saved successfully: originalFilename={}, storedFilename={}, activeUploadDir={}, tenantUploadDir={}, targetPath={}, fileExistsAfterSave={}, fileSizeAfterSaveBytes={}",
                file.getOriginalFilename(),
                storedPath,
                itemUploadRoot,
                tenantDirectory,
                target.toAbsolutePath().normalize(),
                fileExistsAfterSave,
                fileSizeAfterSaveBytes
            );
        } catch (IOException e) {
            log.error(
                "Image upload failed: originalFilename={}, storedFilename={}, activeUploadDir={}, tenantUploadDir={}, targetPath={}, dirExistsBeforeSave={}, dirIsDirectoryBeforeSave={}, dirIsWritableBeforeSave={}",
                file.getOriginalFilename(),
                storedPath,
                itemUploadRoot,
                tenantDirectory,
                target.toAbsolutePath().normalize(),
                directoryExistsBeforeSave,
                directoryIsDirectoryBeforeSave,
                directoryIsWritableBeforeSave,
                e
            );
            throw new IllegalStateException("Could not save image.", e);
        }
        return storedPath;
    }

    public Path getActiveUploadRoot() {
        return uploadRoot;
    }

    public Path getConfiguredUploadRoot() {
        return configuredUploadRoot;
    }

    public boolean isUsingFallbackStorage() {
        return usingFallbackStorage;
    }

    public Optional<Path> resolveImagePath(String storedImagePath, Shop shop) {
        if (!StringUtils.hasText(storedImagePath)) {
            return Optional.empty();
        }

        String normalizedStoredPath = normalizeStoredPath(Paths.get(storedImagePath.replace('\\', '/')));
        String filename = Paths.get(normalizedStoredPath).getFileName().toString();
        LinkedHashSet<Path> candidates = new LinkedHashSet<>();
        String expectedTenantDirectory = resolveShopDirectoryName(shop);

        if (normalizedStoredPath.contains("/")) {
            Path relativePath = Paths.get(normalizedStoredPath).normalize();
            if (!relativePath.isAbsolute() && !relativePath.startsWith("..")) {
                if (shop == null || relativePath.startsWith(expectedTenantDirectory)) {
                    addCandidate(candidates, itemUploadRoot.resolve(relativePath));
                } else {
                    log.warn("Rejected stored image path {} because it does not match expected tenant directory {}.",
                        normalizedStoredPath, expectedTenantDirectory);
                }
            } else {
                log.warn("Rejected unsafe stored image path {}.", storedImagePath);
            }
        }

        if (shop != null) {
            addCandidate(candidates, resolveTenantUploadDirectory(shop).resolve(filename));
        }
        addCandidate(candidates, itemUploadRoot.resolve(filename));
        addCandidate(candidates, uploadRoot.resolve(filename));
        addCandidate(candidates, configuredUploadRoot.resolve(filename));

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Optional.of(candidate);
            }
        }

        return Optional.empty();
    }

    private UploadRootSelection initializeUploadRoot(Path configuredRoot) {
        if (ensureWritableDirectory(configuredRoot)) {
            return new UploadRootSelection(configuredRoot, false);
        }
        Path mountedParentRoot = configuredRoot.getParent();
        if (mountedParentRoot != null && ensureWritableDirectory(mountedParentRoot)) {
            log.warn("Using mounted parent upload directory {} because configured directory {} is not writable.", mountedParentRoot, configuredRoot);
            return new UploadRootSelection(mountedParentRoot, false);
        }
        if (ensureWritableDirectory(FALLBACK_UPLOAD_ROOT)) {
            log.warn("Using fallback upload directory {} because configured directory {} is not writable.", FALLBACK_UPLOAD_ROOT, configuredRoot);
            return new UploadRootSelection(FALLBACK_UPLOAD_ROOT, true);
        }
        throw new IllegalStateException("Could not initialize an upload directory.");
    }

    private Path resolveItemUploadRoot(Path activeRoot, Path configuredRoot) {
        if (activeRoot.equals(configuredRoot)) {
            return activeRoot;
        }
        Path configuredLeaf = configuredRoot.getFileName();
        if (configuredLeaf != null
            && configuredRoot.startsWith(activeRoot)
            && configuredRoot.getNameCount() == activeRoot.getNameCount() + 1) {
            return activeRoot.resolve(configuredLeaf.toString()).toAbsolutePath().normalize();
        }
        if (ITEM_DIRECTORY_NAME.equalsIgnoreCase(activeRoot.getFileName() != null ? activeRoot.getFileName().toString() : "")) {
            return activeRoot;
        }
        return activeRoot.resolve(ITEM_DIRECTORY_NAME).toAbsolutePath().normalize();
    }

    private Path resolveTenantUploadDirectory(Shop shop) {
        return itemUploadRoot.resolve(resolveShopDirectoryName(shop));
    }

    private String resolveShopDirectoryName(Shop shop) {
        if (shop != null && shop.getId() != null) {
            return shop.getId().toString();
        }
        if (shop != null && StringUtils.hasText(shop.getSlug())) {
            return shop.getSlug().trim().replaceAll("[^a-zA-Z0-9_-]", "-");
        }
        return "legacy";
    }

    private String normalizeStoredPath(Path relativePath) {
        return relativePath.toString().replace('\\', '/');
    }

    private void addCandidate(Set<Path> candidates, Path candidate) {
        if (candidate == null) {
            return;
        }
        Path normalized = candidate.toAbsolutePath().normalize();
        candidates.add(normalized);
    }

    private boolean ensureWritableDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
            logPathState("Writable path probe", directory);
            return true;
        } catch (Exception ex) {
            log.warn("Upload path probe failed for {} (exists={}, isDirectory={}, isWritable={})",
                directory,
                Files.exists(directory),
                Files.isDirectory(directory),
                Files.isWritable(directory),
                ex);
            return false;
        }
    }

    private void logPathState(String label, Path directory) {
        log.info("{}: {} (exists={}, isDirectory={}, isWritable={})",
            label,
            directory,
            Files.exists(directory),
            Files.isDirectory(directory),
            Files.isWritable(directory));
    }

    private record UploadRootSelection(Path path, boolean fallback) {
    }
}
