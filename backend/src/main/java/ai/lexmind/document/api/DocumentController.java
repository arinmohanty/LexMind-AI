package ai.lexmind.document.api;

import ai.lexmind.common.security.SecurityUtils;
import ai.lexmind.common.web.ApiResponse;
import ai.lexmind.document.api.dto.DocumentDtos.DocumentDto;
import ai.lexmind.document.service.DocumentService;
import ai.lexmind.document.service.DocumentService.DownloadResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Documents", description = "Upload, list, and retrieve case documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/api/v1/cases/{caseId}/documents",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('document:upload')")
    @Operation(summary = "Upload a document to a case (PDF/DOCX/image, OCR applied downstream)")
    public ApiResponse<DocumentDto> upload(@PathVariable UUID caseId,
                                           @RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(documentService.upload(caseId, file, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/cases/{caseId}/documents")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "List documents in a case")
    public ApiResponse<List<DocumentDto>> list(@PathVariable UUID caseId) {
        return ApiResponse.ok(documentService.list(caseId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/documents/{documentId}/status")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Get processing status of a document")
    public ApiResponse<DocumentDto> status(@PathVariable UUID documentId) {
        return ApiResponse.ok(documentService.getStatus(documentId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/documents/{documentId}/download")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Download the original document")
    public ResponseEntity<Resource> download(@PathVariable UUID documentId) {
        DownloadResult result = documentService.download(documentId, SecurityUtils.currentUser());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        result.mimeType() != null ? result.mimeType() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + result.filename() + "\"")
                .body(result.resource());
    }
}
