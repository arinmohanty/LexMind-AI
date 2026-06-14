package ai.lexmind.analytics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "case_strength")
@Getter
@Setter
@NoArgsConstructor
public class CaseStrength {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "analysis_run_id")
    private UUID analysisRunId;

    @Column(name = "overall_score")
    private BigDecimal overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "findings_json", columnDefinition = "jsonb")
    private String findingsJson;   // {strong[], weak[], missingEvidence[], openQuestions[]}

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
