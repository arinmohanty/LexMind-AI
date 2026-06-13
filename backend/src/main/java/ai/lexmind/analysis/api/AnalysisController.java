package ai.lexmind.analysis.api;

import ai.lexmind.analysis.api.dto.AnalysisDtos.AnalysisRunDto;
import ai.lexmind.analysis.service.AnalysisService;
import ai.lexmind.common.security.SecurityUtils;
import ai.lexmind.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Analysis", description = "Trigger and track AI analysis runs")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/api/v1/cases/{caseId}/analyze")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('analysis:run')")
    @Operation(summary = "Start an async AI analysis run for a case")
    public ApiResponse<AnalysisRunDto> analyze(@PathVariable UUID caseId) {
        return ApiResponse.ok(analysisService.startAnalysis(caseId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/analysis/{runId}")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Get the status + agent telemetry of an analysis run (poll until COMPLETED)")
    public ApiResponse<AnalysisRunDto> getRun(@PathVariable UUID runId) {
        return ApiResponse.ok(analysisService.getRun(runId, SecurityUtils.currentUser()));
    }
}
