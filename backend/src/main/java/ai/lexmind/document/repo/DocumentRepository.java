package ai.lexmind.document.repo;

import ai.lexmind.document.domain.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByCaseIdOrderByCreatedAtDesc(UUID caseId);
    long countByCaseIdAndStatus(UUID caseId, String status);
}
