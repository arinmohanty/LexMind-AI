package ai.lexmind.casefile.service;

import ai.lexmind.casefile.domain.CaseFile;
import ai.lexmind.casefile.repo.CaseRepository;
import ai.lexmind.common.error.AppExceptions.NotFoundException;
import ai.lexmind.common.security.UserPrincipal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseAccessServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @InjectMocks
    private CaseAccessService accessService;

    private static UserPrincipal principal(String role, UUID id, UUID org) {
        return new UserPrincipal(id, "u@example.com", null, org, role, true, List.of());
    }

    private static CaseFile caseFile(UUID owner, UUID org, String status) {
        CaseFile c = new CaseFile();
        c.setOwnerId(owner);
        c.setOrganizationId(org);
        c.setStatus(status);
        return c;
    }

    @Test
    void ownerCanAccessOwnCase() {
        UUID userId = UUID.randomUUID();
        CaseFile c = caseFile(userId, null, "ACTIVE");
        assertThat(accessService.canAccess(c, principal("ADVOCATE", userId, null))).isTrue();
    }

    @Test
    void otherUserCannotAccess() {
        CaseFile c = caseFile(UUID.randomUUID(), null, "ACTIVE");
        assertThat(accessService.canAccess(c, principal("ADVOCATE", UUID.randomUUID(), null))).isFalse();
    }

    @Test
    void firmAdminCanAccessSameOrgCase() {
        UUID org = UUID.randomUUID();
        CaseFile c = caseFile(UUID.randomUUID(), org, "ACTIVE");
        assertThat(accessService.canAccess(c, principal("LAW_FIRM_ADMIN", UUID.randomUUID(), org))).isTrue();
    }

    @Test
    void firmAdminCannotAccessOtherOrgCase() {
        CaseFile c = caseFile(UUID.randomUUID(), UUID.randomUUID(), "ACTIVE");
        assertThat(accessService.canAccess(c, principal("LAW_FIRM_ADMIN", UUID.randomUUID(), UUID.randomUUID())))
                .isFalse();
    }

    @Test
    void superAdminCanAccessAnything() {
        CaseFile c = caseFile(UUID.randomUUID(), null, "DELETED");
        assertThat(accessService.canAccess(c, principal("SUPER_ADMIN", UUID.randomUUID(), null))).isTrue();
    }

    @Test
    void deletedCaseHiddenFromOwner() {
        UUID userId = UUID.randomUUID();
        CaseFile c = caseFile(userId, null, "DELETED");
        assertThat(accessService.canAccess(c, principal("ADVOCATE", userId, null))).isFalse();
    }

    @Test
    void getAccessibleCaseThrowsNotFoundWhenDenied() {
        UUID caseId = UUID.randomUUID();
        CaseFile c = caseFile(UUID.randomUUID(), null, "ACTIVE");
        when(caseRepository.findByIdWithParties(caseId)).thenReturn(Optional.of(c));

        assertThatThrownBy(() ->
                accessService.getAccessibleCase(caseId, principal("ADVOCATE", UUID.randomUUID(), null)))
                .isInstanceOf(NotFoundException.class);
    }
}
