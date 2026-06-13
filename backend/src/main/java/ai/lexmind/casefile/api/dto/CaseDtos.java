package ai.lexmind.casefile.api.dto;

import ai.lexmind.casefile.domain.CaseFile;
import ai.lexmind.casefile.domain.CaseParty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class CaseDtos {

    private CaseDtos() {}

    public record PartyInput(
            @NotBlank @Size(max = 200) String name,
            @NotBlank String side,
            String counsel
    ) {}

    public record CreateCaseRequest(
            @NotBlank @Size(max = 300) String title,
            String caseNumber,
            String court,
            String jurisdiction,
            String caseType,
            String stage,
            LocalDate filingDate,
            @Valid List<PartyInput> parties
    ) {}

    public record CasePartyDto(UUID id, String name, String side, String counsel) {
        public static CasePartyDto from(CaseParty p) {
            return new CasePartyDto(p.getId(), p.getName(), p.getSide(), p.getCounsel());
        }
    }

    /** Compact projection for list/repository views. */
    public record CaseSummaryDto(
            UUID id, String title, String caseNumber, String court,
            String caseType, String stage, String status, Instant updatedAt
    ) {
        public static CaseSummaryDto from(CaseFile c) {
            return new CaseSummaryDto(c.getId(), c.getTitle(), c.getCaseNumber(), c.getCourt(),
                    c.getCaseType(), c.getStage(), c.getStatus(), c.getUpdatedAt());
        }
    }

    /** Full case detail including parties. */
    public record CaseDto(
            UUID id, UUID ownerId, UUID organizationId, String title, String caseNumber,
            String court, String jurisdiction, String caseType, String stage,
            LocalDate filingDate, String status, List<CasePartyDto> parties,
            Instant createdAt, Instant updatedAt
    ) {
        public static CaseDto from(CaseFile c) {
            return new CaseDto(c.getId(), c.getOwnerId(), c.getOrganizationId(), c.getTitle(),
                    c.getCaseNumber(), c.getCourt(), c.getJurisdiction(), c.getCaseType(),
                    c.getStage(), c.getFilingDate(), c.getStatus(),
                    c.getParties().stream().map(CasePartyDto::from).toList(),
                    c.getCreatedAt(), c.getUpdatedAt());
        }
    }
}
