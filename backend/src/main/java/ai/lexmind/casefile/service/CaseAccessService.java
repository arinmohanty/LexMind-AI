package ai.lexmind.casefile.service;

import ai.lexmind.casefile.domain.CaseFile;
import ai.lexmind.casefile.repo.CaseRepository;
import ai.lexmind.common.error.AppExceptions.NotFoundException;
import ai.lexmind.common.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

/**
 * Central enforcement of per-case access (PRD RBAC scopes). Reused by documents, analysis,
 * and dashboard modules so isolation logic lives in exactly one place (ADR-0009).
 */
@Service
public class CaseAccessService {

    private final CaseRepository caseRepository;

    public CaseAccessService(CaseRepository caseRepository) {
        this.caseRepository = caseRepository;
    }

    @Transactional(readOnly = true)
    public CaseFile getAccessibleCase(UUID caseId, UserPrincipal user) {
        CaseFile c = caseRepository.findByIdWithParties(caseId)
                .orElseThrow(() -> new NotFoundException("CASE_NOT_FOUND", "Case not found"));
        assertCanAccess(c, user);
        return c;
    }

    public void assertCanAccess(CaseFile c, UserPrincipal user) {
        if (canAccess(c, user)) {
            return;
        }
        // Hide existence from unauthorized users.
        throw new NotFoundException("CASE_NOT_FOUND", "Case not found");
    }

    public boolean canAccess(CaseFile c, UserPrincipal user) {
        String role = user.getRole();
        if ("SUPER_ADMIN".equals(role)) {
            return true;
        }
        if ("DELETED".equals(c.getStatus())) {
            return false;
        }
        if (Objects.equals(c.getOwnerId(), user.getId())) {
            return true;
        }
        return "LAW_FIRM_ADMIN".equals(role)
                && c.getOrganizationId() != null
                && Objects.equals(c.getOrganizationId(), user.getOrganizationId());
    }
}
