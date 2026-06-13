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
@Table(name = "case_facts")
@Getter
@Setter
@NoArgsConstructor
public class CaseFact {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "analysis_run_id")
    private UUID analysisRunId;

    @Column(name = "fact_text", nullable = false)
    private String factText;

    @Column(name = "fact_status", nullable = false)
    private String factStatus;   // ESTABLISHED | DISPUTED | MISSING

    @Column(name = "source_excerpt")
    private String sourceExcerpt;

    @Column(name = "confidence")
    private BigDecimal confidence;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
