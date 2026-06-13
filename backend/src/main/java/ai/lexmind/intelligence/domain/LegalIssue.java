package ai.lexmind.intelligence.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "legal_issues")
@Getter
@Setter
@NoArgsConstructor
public class LegalIssue {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "analysis_run_id")
    private UUID analysisRunId;

    @Column(name = "issue_text", nullable = false)
    private String issueText;

    @Column(name = "issue_type", nullable = false)
    private String issueType = "PRIMARY";   // PRIMARY | SECONDARY

    @Column(name = "rank", nullable = false)
    private int rank;

    @Column(name = "importance_score")
    private BigDecimal importanceScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
