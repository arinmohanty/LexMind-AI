package ai.lexmind.casefile.api;

import ai.lexmind.casefile.api.dto.CaseDtos.CaseDto;
import ai.lexmind.casefile.api.dto.CaseDtos.CaseSummaryDto;
import ai.lexmind.casefile.api.dto.CaseDtos.CreateCaseRequest;
import ai.lexmind.casefile.service.CaseService;
import ai.lexmind.common.security.SecurityUtils;
import ai.lexmind.common.web.ApiResponse;
import ai.lexmind.common.web.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cases")
@Tag(name = "Cases", description = "Case (matter) workspace management")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "List cases visible to the current user (role-scoped)")
    public ApiResponse<PageResponse<CaseSummaryDto>> list(
            @PageableDefault(size = 20) Pageable pageable) {
        return ApiResponse.ok(caseService.list(SecurityUtils.currentUser(), pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('case:create')")
    @Operation(summary = "Create a new case (matter)")
    public ApiResponse<CaseDto> create(@Valid @RequestBody CreateCaseRequest req) {
        return ApiResponse.ok(caseService.create(req, SecurityUtils.currentUser()));
    }

    @GetMapping("/{caseId}")
    @PreAuthorize("hasAuthority('case:read')")
    @Operation(summary = "Get a case by id")
    public ApiResponse<CaseDto> get(@PathVariable UUID caseId) {
        return ApiResponse.ok(caseService.get(caseId, SecurityUtils.currentUser()));
    }

    @DeleteMapping("/{caseId}")
    @PreAuthorize("hasAuthority('case:delete')")
    @Operation(summary = "Archive (soft-delete) a case")
    public ApiResponse<Void> archive(@PathVariable UUID caseId) {
        caseService.archive(caseId, SecurityUtils.currentUser());
        return ApiResponse.ok(null);
    }
}
