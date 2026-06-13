package ai.lexmind.analysis.service;

import ai.lexmind.analysis.client.AiContracts.AgentResultsPayload;
import ai.lexmind.analysis.client.AiContracts.RunAgentsRequest;
import ai.lexmind.analysis.client.AiServiceClient;
import ai.lexmind.document.domain.Document;
import ai.lexmind.document.repo.DocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Runs an analysis asynchronously (ADR-0007): mark PROCESSING → call the AI agent graph →
 * persist results, or mark FAILED on error. Keeps the API request path responsive.
 */
@Component
public class AnalysisOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AnalysisOrchestrator.class);

    private final AiServiceClient aiServiceClient;
    private final AnalysisIngestService ingestService;
    private final DocumentRepository documentRepository;

    public AnalysisOrchestrator(AiServiceClient aiServiceClient, AnalysisIngestService ingestService,
                                DocumentRepository documentRepository) {
        this.aiServiceClient = aiServiceClient;
        this.ingestService = ingestService;
        this.documentRepository = documentRepository;
    }

    @Async
    public void run(UUID runId, UUID caseId) {
        try {
            ingestService.markProcessing(runId);
            List<UUID> documentIds = documentRepository.findByCaseIdOrderByCreatedAtDesc(caseId)
                    .stream().map(Document::getId).toList();
            AgentResultsPayload payload = aiServiceClient.runAgents(
                    new RunAgentsRequest(caseId, documentIds, Map.of()));
            ingestService.ingest(runId, payload);
            log.info("Analysis run {} completed for case {}", runId, caseId);
        } catch (Exception ex) {
            log.error("Analysis run {} failed for case {}", runId, caseId, ex);
            ingestService.markFailed(runId, ex.getMessage());
        }
    }
}
