package ai.lexmind.analysis.api;

import ai.lexmind.analysis.client.AiContracts.AgentResultsPayload;
import ai.lexmind.analysis.service.AnalysisIngestService;
import ai.lexmind.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Server-to-server callback for the AI service's async (push) result delivery. Authenticated
 * via the internal service token ({@code ROLE_INTERNAL}); never exposed to end users.
 */
@RestController
@Tag(name = "Internal", description = "Service-to-service callbacks (AI tier)")
public class InternalAnalysisController {

    private final AnalysisIngestService ingestService;

    public InternalAnalysisController(AnalysisIngestService ingestService) {
        this.ingestService = ingestService;
    }

    @PostMapping("/internal/analysis/{runId}/results")
    @PreAuthorize("hasRole('INTERNAL')")
    @Operation(summary = "AI service pushes structured analysis results for a run")
    public ApiResponse<Map<String, String>> ingest(@PathVariable UUID runId,
                                                    @RequestBody AgentResultsPayload payload) {
        ingestService.ingest(runId, payload);
        return ApiResponse.ok(Map.of("status", "ingested"));
    }
}
