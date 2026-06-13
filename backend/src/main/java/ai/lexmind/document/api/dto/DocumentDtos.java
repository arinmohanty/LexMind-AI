package ai.lexmind.document.api.dto;

import ai.lexmind.document.domain.Document;

import java.time.Instant;
import java.util.UUID;

public final class DocumentDtos {

    private DocumentDtos() {}

    public record DocumentDto(
            UUID id, UUID caseId, String originalFilename, String mimeType, long sizeBytes,
            String docType, String status, int pageCount, boolean ocrApplied,
            String errorMessage, Instant createdAt
    ) {
        public static DocumentDto from(Document d) {
            return new DocumentDto(d.getId(), d.getCaseId(), d.getOriginalFilename(), d.getMimeType(),
                    d.getSizeBytes(), d.getDocType(), d.getStatus(), d.getPageCount(),
                    d.isOcrApplied(), d.getErrorMessage(), d.getCreatedAt());
        }
    }
}
