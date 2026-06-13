package ai.lexmind.document.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Object-storage abstraction (ADR-0012). Local FS in dev; S3-compatible in prod.
 */
public interface StorageService {

    StoredObject store(UUID caseId, MultipartFile file);

    Resource load(String storageKey);

    void delete(String storageKey);

    /** Result of storing a file. */
    record StoredObject(String storageKey, String checksum, long sizeBytes) {}
}
