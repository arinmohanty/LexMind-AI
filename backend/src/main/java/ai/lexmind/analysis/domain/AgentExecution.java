package ai.lexmind.analysis.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Per-agent telemetry + raw output for one analysis run (maps to {@code agent_executions}). */
@Entity
@Table(name = "agent_executions")
@Getter
@Setter
@NoArgsConstructor
public class AgentExecution {

    @Id
    @Column(name = "id")
    private UUID id = UUID.randomUUID();

    @Column(name = "analysis_run_id", nullable = false)
    private UUID analysisRunId;

    @Column(name = "agent_type", nullable = false)
    private String agentType;   // FACT_EXTRACTION | ISSUE_IDENTIFICATION | ...

    @Column(name = "status", nullable = false)
    private String status = "COMPLETED";

    @Column(name = "latency_ms")
    private Integer latencyMs;

    @Column(name = "tokens")
    private long tokens;

    @Column(name = "model")
    private String model;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_json", columnDefinition = "jsonb")
    private String outputJson;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
