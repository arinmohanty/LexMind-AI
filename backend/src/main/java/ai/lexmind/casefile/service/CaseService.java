package ai.lexmind.casefile.service;

import ai.lexmind.casefile.api.dto.CaseDtos.CaseDto;
import ai.lexmind.casefile.api.dto.CaseDtos.CaseSummaryDto;
import ai.lexmind.casefile.api.dto.CaseDtos.CreateCaseRequest;
import ai.lexmind.casefile.domain.CaseFile;
import ai.lexmind.casefile.domain.CaseParty;
import ai.lexmind.casefile.repo.CaseRepository;
import ai.lexmind.common.audit.AuditService;
import ai.lexmind.common.security.UserPrincipal;
import ai.lexmind.common.web.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CaseService {

    private static final String NOT_DELETED = "DELETED";

    private final CaseRepository caseRepository;
    private final CaseAccessService accessService;
    private final AuditService auditService;

    public CaseService(CaseRepository caseRepository, CaseAccessService accessService,
                       AuditService auditService) {
        this.caseRepository = caseRepository;
        this.accessService = accessService;
        this.auditService = auditService;
    }

    @Transactional
    public CaseDto create(CreateCaseRequest req, UserPrincipal user) {
        CaseFile c = new CaseFile();
        c.setOwnerId(user.getId());
        c.setOrganizationId(user.getOrganizationId());
        c.setTitle(req.title());
        c.setCaseNumber(req.caseNumber());
        c.setCourt(req.court());
        c.setJurisdiction(req.jurisdiction());
        c.setCaseType(req.caseType());
        if (req.stage() != null) {
            c.setStage(req.stage());
        }
        c.setFilingDate(req.filingDate());
        if (req.parties() != null) {
            req.parties().forEach(p -> {
                CaseParty party = new CaseParty();
                party.setName(p.name());
                party.setSide(p.side());
                party.setCounsel(p.counsel());
                c.addParty(party);
            });
        }
        caseRepository.save(c);
        auditService.record(user, "CASE_CREATED", "CASE", c.getId(), null);
        return CaseDto.from(c);
    }

    @Transactional(readOnly = true)
    public PageResponse<CaseSummaryDto> list(UserPrincipal user, Pageable pageable) {
        Page<CaseFile> page;
        if ("SUPER_ADMIN".equals(user.getRole())) {
            page = caseRepository.findAll(pageable);
        } else if ("LAW_FIRM_ADMIN".equals(user.getRole()) && user.getOrganizationId() != null) {
            page = caseRepository.findByOrganizationIdAndStatusNot(
                    user.getOrganizationId(), NOT_DELETED, pageable);
        } else {
            page = caseRepository.findByOwnerIdAndStatusNot(user.getId(), NOT_DELETED, pageable);
        }
        return PageResponse.from(page, CaseSummaryDto::from);
    }

    @Transactional(readOnly = true)
    public CaseDto get(UUID caseId, UserPrincipal user) {
        return CaseDto.from(accessService.getAccessibleCase(caseId, user));
    }

    @Transactional
    public void archive(UUID caseId, UserPrincipal user) {
        CaseFile c = accessService.getAccessibleCase(caseId, user);
        c.setStatus("ARCHIVED");
        auditService.record(user, "CASE_ARCHIVED", "CASE", c.getId(), null);
    }
}
