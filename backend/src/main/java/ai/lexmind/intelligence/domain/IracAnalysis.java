package ai.lexmind.intelligence.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "irac_analyses")
@Getter
@Setter
@NoArgsConstructor
public class IracAnalysis {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "issue", nullable = false)
    private String issue;

    @Column(name = "rule", nullable = false)
    private String rule;

    @Column(name = "application", nullable = false)
    private String application;

    @Column(name = "conclusion", nullable = false)
    private String conclusion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
