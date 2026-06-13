package ai.lexmind.intelligence.api;

import ai.lexmind.common.security.SecurityUtils;
import ai.lexmind.common.web.ApiResponse;
import ai.lexmind.intelligence.api.dto.DashboardDtos.ArgumentsView;
import ai.lexmind.intelligence.api.dto.DashboardDtos.FactDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.IracDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.IssueDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.OverviewDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.TimelineEventDto;
import ai.lexmind.intelligence.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Case Dashboard", description = "Read-side legal intelligence for the Case Analysis Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/api/v1/cases/{caseId}/dashboard/overview")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Case overview: snapshot, parties, fact/issue counts, latest run status")
    public ApiResponse<OverviewDto> overview(@PathVariable UUID caseId) {
        return ApiResponse.ok(dashboardService.overview(caseId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/cases/{caseId}/dashboard/facts")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Fact matrix (optionally filter by status: ESTABLISHED/DISPUTED/MISSING)")
    public ApiResponse<List<FactDto>> facts(@PathVariable UUID caseId,
                                            @RequestParam(required = false) String status) {
        return ApiResponse.ok(dashboardService.facts(caseId, SecurityUtils.currentUser(), status));
    }

    @GetMapping("/api/v1/cases/{caseId}/dashboard/timeline")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Case chronology")
    public ApiResponse<List<TimelineEventDto>> timeline(@PathVariable UUID caseId) {
        return ApiResponse.ok(dashboardService.timeline(caseId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/cases/{caseId}/dashboard/issues")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Ranked legal issues")
    public ApiResponse<List<IssueDto>> issues(@PathVariable UUID caseId) {
        return ApiResponse.ok(dashboardService.issues(caseId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/cases/{caseId}/dashboard/arguments")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Petitioner vs respondent arguments (side-by-side)")
    public ApiResponse<ArgumentsView> arguments(@PathVariable UUID caseId) {
        return ApiResponse.ok(dashboardService.arguments(caseId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/cases/{caseId}/irac")
    @PreAuthorize("hasAuthority('irac:view')")
    @Operation(summary = "IRAC analysis (Issue / Rule / Application / Conclusion)")
    public ApiResponse<List<IracDto>> irac(@PathVariable UUID caseId) {
        return ApiResponse.ok(dashboardService.irac(caseId, SecurityUtils.currentUser()));
    }
}
