package ai.lexmind.analysis.service;

import ai.lexmind.analysis.api.dto.AnalysisDtos.AnalysisRunDto;
import ai.lexmind.analysis.domain.AnalysisRun;
import ai.lexmind.analysis.repo.AnalysisRunRepository;
import ai.lexmind.casefile.service.CaseAccessService;
import ai.lexmind.common.audit.AuditService;
import ai.lexmind.common.error.AppExceptions.NotFoundException;
import ai.lexmind.common.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AnalysisService {

    private final AnalysisRunRepository runRepository;
    private final CaseAccessService accessService;
    private final AnalysisOrchestrator orchestrator;
    private final AnalysisIngestService ingestService;
    private final AuditService auditService;

    public AnalysisService(AnalysisRunRepository runRepository, CaseAccessService accessService,
                           AnalysisOrchestrator orchestrator, AnalysisIngestService ingestService,
                           AuditService auditService) {
        this.runRepository = runRepository;
        this.accessService = accessService;
        this.orchestrator = orchestrator;
        this.ingestService = ingestService;
        this.auditService = auditService;
    }

    /**
     * Not wrapped in a transaction: the run is committed by {@code save} before the async
     * orchestrator starts, so the worker can never read it before commit.
     */
    public AnalysisRunDto startAnalysis(UUID caseId, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);   // RBAC + existence
        AnalysisRun run = new AnalysisRun();
        run.setCaseId(caseId);
        run.setTriggeredBy(user.getId());
        run.setStatus("QUEUED");
        runRepository.save(run);

        auditService.record(user, "ANALYSIS_STARTED", "ANALYSIS_RUN", run.getId(), null);
        orchestrator.run(run.getId(), caseId);           // async
        return AnalysisRunDto.from(run, List.of());
    }

    @Transactional(readOnly = true)
    public AnalysisRunDto getRun(UUID runId, UserPrincipal user) {
        AnalysisRun run = runRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("RUN_NOT_FOUND", "Analysis run not found"));
        accessService.getAccessibleCase(run.getCaseId(), user);   // enforce case-level access
        return AnalysisRunDto.from(run, ingestService.agentsForRun(runId));
    }
}
