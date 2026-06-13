package ai.lexmind.document.storage;

import ai.lexmind.common.config.LexMindProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Filesystem-backed storage (dev/default). Files live under {@code <localPath>/<caseId>/}.
 * Active unless {@code lexmind.storage.backend=s3} (an S3 impl would be added in Phase 9).
 */
@Service
@ConditionalOnProperty(name = "lexmind.storage.backend", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path basePath;

    public LocalStorageService(LexMindProperties props) {
        this.basePath = Paths.get(props.storage().localPath()).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject store(UUID caseId, MultipartFile file) {
        try {
            Path dir = basePath.resolve(caseId.toString());
            Files.createDirectories(dir);
            String safeName = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
            Path target = dir.resolve(safeName).normalize();
            if (!target.startsWith(basePath)) {
                throw new IllegalStateException("Resolved path escapes storage root");
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = file.getInputStream();
                 DigestInputStream dis = new DigestInputStream(in, digest)) {
                Files.copy(dis, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String checksum = HexFormat.of().formatHex(digest.digest());
            String key = caseId + "/" + safeName;
            return new StoredObject(key, checksum, Files.size(target));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    @Override
    public Resource load(String storageKey) {
        try {
            Path path = basePath.resolve(storageKey).normalize();
            if (!path.startsWith(basePath)) {
                throw new IllegalStateException("Invalid storage key");
            }
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("File not found: " + storageKey);
            }
            return resource;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load file", e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path path = basePath.resolve(storageKey).normalize();
            if (path.startsWith(basePath)) {
                Files.deleteIfExists(path);
            }
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private static String sanitize(String name) {
        if (name == null || name.isBlank()) {
            return "upload";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
