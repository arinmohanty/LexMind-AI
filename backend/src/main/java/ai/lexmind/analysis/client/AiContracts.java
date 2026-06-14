package ai.lexmind.analysis.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Wire contract between the backend and the FastAPI AI service (ADR-0002 / AI architecture).
 * Mirrors the structured payload each agent produces.
 */
public final class AiContracts {

    private AiContracts() {}

    public record RunAgentsRequest(UUID caseId, List<UUID> documentIds, Map<String, Object> options) {}

    public record FactPayload(String factText, String factStatus, String sourceExcerpt,
                              BigDecimal confidence) {}

    public record TimelinePayload(LocalDate eventDate, String eventText, String eventType,
                                  Integer sortOrder) {}

    public record IssuePayload(String issueText, String issueType, Integer rank,
                               BigDecimal importanceScore) {}

    public record ArgumentPayload(String partySide, String argumentText, String strength,
                                  String sourceExcerpt) {}

    public record IracPayload(String issue, String rule, String application, String conclusion) {}

    public record CaseStrengthPayload(Double overallScore, List<String> strong, List<String> weak,
                                      List<String> missingEvidence, List<String> openQuestions) {}

    public record RiskPayload(String riskType, String severity, String description) {}

    public record ReadinessPayload(Double evidenceReadiness, Double witnessReadiness,
                                   Double researchReadiness, Double hearingReadiness,
                                   Double overallReadiness) {}

    public record AgentExecutionPayload(String agentType, String status, Integer latencyMs,
                                        Long tokens, String model, String outputJson,
                                        String errorMessage) {}

    /** Full structured result of one analysis run. */
    public record AgentResultsPayload(
            String model,
            Long totalTokens,
            BigDecimal costUsd,
            List<FactPayload> facts,
            List<TimelinePayload> timeline,
            List<IssuePayload> issues,
            List<ArgumentPayload> arguments,
            List<IracPayload> irac,
            CaseStrengthPayload caseStrength,
            List<RiskPayload> risks,
            ReadinessPayload readiness,
            List<AgentExecutionPayload> agentExecutions
    ) {}

    // ---- RAG chat ----
    public record ChatTurn(String role, String content) {}

    public record ChatRequest(UUID caseId, String question, List<ChatTurn> history) {}

    public record Citation(UUID documentId, UUID chunkId, String excerpt, Integer page) {}

    public record ChatAnswer(String answer, List<Citation> citations, String confidence) {}
}
