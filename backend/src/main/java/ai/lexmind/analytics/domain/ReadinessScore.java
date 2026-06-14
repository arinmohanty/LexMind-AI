package ai.lexmind.analytics.domain;

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
@Table(name = "readiness_scores")
@Getter
@Setter
@NoArgsConstructor
public class ReadinessScore {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "evidence_readiness")
    private BigDecimal evidenceReadiness;

    @Column(name = "witness_readiness")
    private BigDecimal witnessReadiness;

    @Column(name = "research_readiness")
    private BigDecimal researchReadiness;

    @Column(name = "hearing_readiness")
    private BigDecimal hearingReadiness;

    @Column(name = "overall_readiness")
    private BigDecimal overallReadiness;

    @Column(name = "computed_at", nullable = false)
    private Instant computedAt = Instant.now();
}
