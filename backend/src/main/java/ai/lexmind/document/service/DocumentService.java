package ai.lexmind.document.service;

import ai.lexmind.casefile.service.CaseAccessService;
import ai.lexmind.common.audit.AuditService;
import ai.lexmind.common.config.LexMindProperties;
import ai.lexmind.common.error.AppExceptions.BadRequestException;
import ai.lexmind.common.error.AppExceptions.NotFoundException;
import ai.lexmind.common.security.UserPrincipal;
import ai.lexmind.document.api.dto.DocumentDtos.DocumentDto;
import ai.lexmind.document.domain.Document;
import ai.lexmind.document.repo.DocumentRepository;
import ai.lexmind.document.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final StorageService storageService;
    private final CaseAccessService accessService;
    private final AuditService auditService;
    private final LexMindProperties props;

    public DocumentService(DocumentRepository documentRepository, StorageService storageService,
                           CaseAccessService accessService, AuditService auditService,
                           LexMindProperties props) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.accessService = accessService;
        this.auditService = auditService;
        this.props = props;
    }

    @Transactional
    public DocumentDto upload(UUID caseId, MultipartFile file, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);   // RBAC + existence
        validate(file);

        StorageService.StoredObject stored = storageService.store(caseId, file);
        Document doc = new Document();
        doc.setCaseId(caseId);
        doc.setUploadedBy(user.getId());
        doc.setOriginalFilename(file.getOriginalFilename());
        doc.setStorageKey(stored.storageKey());
        doc.setMimeType(file.getContentType());
        doc.setSizeBytes(stored.sizeBytes());
        doc.setChecksum(stored.checksum());
        doc.setStatus("QUEUED");          // picked up by the analysis pipeline
        documentRepository.save(doc);

        auditService.record(user, "DOCUMENT_UPLOADED", "DOCUMENT", doc.getId(), null);
        return DocumentDto.from(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> list(UUID caseId, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);
        return documentRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(DocumentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto getStatus(UUID documentId, UserPrincipal user) {
        Document doc = loadAccessible(documentId, user);
        return DocumentDto.from(doc);
    }

    @Transactional(readOnly = true)
    public DownloadResult download(UUID documentId, UserPrincipal user) {
        Document doc = loadAccessible(documentId, user);
        Resource resource = storageService.load(doc.getStorageKey());
        return new DownloadResult(resource, doc.getOriginalFilename(), doc.getMimeType());
    }

    /** Service-to-service fetch (no user context) used by the AI tier to extract text. */
    @Transactional(readOnly = true)
    public DownloadResult downloadInternal(UUID documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("DOCUMENT_NOT_FOUND", "Document not found"));
        Resource resource = storageService.load(doc.getStorageKey());
        return new DownloadResult(resource, doc.getOriginalFilename(), doc.getMimeType());
    }

    private Document loadAccessible(UUID documentId, UserPrincipal user) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("DOCUMENT_NOT_FOUND", "Document not found"));
        accessService.getAccessibleCase(doc.getCaseId(), user);   // enforce case-level access
        return doc;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("EMPTY_FILE", "Uploaded file is empty");
        }
        if (file.getSize() > props.storage().maxFileBytes()) {
            throw new BadRequestException("FILE_TOO_LARGE", "File exceeds the maximum allowed size");
        }
        String mime = file.getContentType();
        if (mime == null || !props.storage().allowedMimeTypes().contains(mime)) {
            throw new BadRequestException("UNSUPPORTED_TYPE",
                    "Unsupported file type. Allowed: PDF, DOCX, PNG, JPEG, TIFF");
        }
    }

    public record DownloadResult(Resource resource, String filename, String mimeType) {}
}
