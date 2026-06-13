package ai.lexmind.analysis.api.dto;

import ai.lexmind.analysis.domain.AgentExecution;
import ai.lexmind.analysis.domain.AnalysisRun;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class AnalysisDtos {

    private AnalysisDtos() {}

    public record AgentExecutionDto(String agentType, String status, Integer latencyMs, long tokens) {
        public static AgentExecutionDto from(AgentExecution e) {
            return new AgentExecutionDto(e.getAgentType(), e.getStatus(), e.getLatencyMs(), e.getTokens());
        }
    }

    public record AnalysisRunDto(
            UUID id, UUID caseId, String status, String model, long totalTokens,
            BigDecimal costUsd, String errorMessage, Instant startedAt, Instant completedAt,
            Instant createdAt, List<AgentExecutionDto> agents
    ) {
        public static AnalysisRunDto from(AnalysisRun r, List<AgentExecution> agents) {
            return new AnalysisRunDto(r.getId(), r.getCaseId(), r.getStatus(), r.getModel(),
                    r.getTotalTokens(), r.getCostUsd(), r.getErrorMessage(),
                    r.getStartedAt(), r.getCompletedAt(), r.getCreatedAt(),
                    agents.stream().map(AgentExecutionDto::from).toList());
        }
    }
}
