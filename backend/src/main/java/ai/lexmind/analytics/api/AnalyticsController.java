package ai.lexmind.analytics.api;

import ai.lexmind.analytics.api.dto.AnalyticsDtos.CaseAnalyticsDto;
import ai.lexmind.analytics.api.dto.AnalyticsDtos.PortfolioDto;
import ai.lexmind.analytics.service.AnalyticsService;
import ai.lexmind.common.security.SecurityUtils;
import ai.lexmind.common.web.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Analytics", description = "Legal Analytics Center — strength, risk, readiness")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/api/v1/cases/{caseId}/analytics")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Per-case analytics: case strength, risks, and litigation readiness")
    public ApiResponse<CaseAnalyticsDto> caseAnalytics(@PathVariable UUID caseId) {
        return ApiResponse.ok(analyticsService.getCaseAnalytics(caseId, SecurityUtils.currentUser()));
    }

    @GetMapping("/api/v1/analytics/portfolio")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Portfolio analytics across the user's accessible cases")
    public ApiResponse<PortfolioDto> portfolio() {
        return ApiResponse.ok(analyticsService.getPortfolio(SecurityUtils.currentUser()));
    }
}
