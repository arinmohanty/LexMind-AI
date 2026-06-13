package ai.lexmind.analysis.domain;

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

/** A single versioned AI analysis pass over a case (maps to {@code analysis_runs}). */
@Entity
@Table(name = "analysis_runs")
@Getter
@Setter
@NoArgsConstructor
public class AnalysisRun {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "triggered_by")
    private UUID triggeredBy;

    @Column(name = "status", nullable = false)
    private String status = "QUEUED";   // QUEUED | PROCESSING | COMPLETED | FAILED | PARTIAL

    @Column(name = "model")
    private String model;

    @Column(name = "total_tokens")
    private long totalTokens;

    @Column(name = "cost_usd")
    private BigDecimal costUsd = BigDecimal.ZERO;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
