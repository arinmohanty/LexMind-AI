package ai.lexmind.casefile.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** A matter — the central workspace entity (maps to {@code cases}). */
@Entity
@Table(name = "cases")
@Getter
@Setter
@NoArgsConstructor
public class CaseFile {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "case_number")
    private String caseNumber;

    @Column(name = "court")
    private String court;

    @Column(name = "jurisdiction")
    private String jurisdiction;

    @Column(name = "case_type")
    private String caseType;     // CRIMINAL | CIVIL | ... (validated at DTO layer)

    @Column(name = "stage")
    private String stage = "INTAKE";

    @Column(name = "filing_date")
    private LocalDate filingDate;

    @Column(name = "status", nullable = false)
    private String status = "ACTIVE";    // ACTIVE | ARCHIVED | DELETED

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "caseFile", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<CaseParty> parties = new ArrayList<>();

    public void addParty(CaseParty party) {
        party.setCaseFile(this);
        this.parties.add(party);
    }
}
