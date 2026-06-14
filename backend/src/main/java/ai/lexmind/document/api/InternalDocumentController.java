package ai.lexmind.document.api;

import ai.lexmind.document.service.DocumentService;
import ai.lexmind.document.service.DocumentService.DownloadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Service-to-service endpoint: the AI tier fetches original document bytes to extract text.
 * Authenticated via the internal service token ({@code ROLE_INTERNAL}); never user-facing.
 */
@RestController
@Tag(name = "Internal", description = "Service-to-service document access (AI tier)")
public class InternalDocumentController {

    private final DocumentService documentService;

    public InternalDocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/internal/documents/{documentId}/content")
    @PreAuthorize("hasRole('INTERNAL')")
    @Operation(summary = "Fetch original document bytes for AI processing")
    public ResponseEntity<Resource> content(@PathVariable UUID documentId) {
        DownloadResult result = documentService.downloadInternal(documentId);
        String mime = result.mimeType() != null ? result.mimeType() : "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .header("X-Filename", result.filename())
                .body(result.resource());
    }
}
