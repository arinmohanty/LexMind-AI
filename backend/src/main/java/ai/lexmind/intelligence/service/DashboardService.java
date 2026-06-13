package ai.lexmind.intelligence.service;

import ai.lexmind.analysis.service.AnalysisIngestService;
import ai.lexmind.casefile.domain.CaseFile;
import ai.lexmind.casefile.service.CaseAccessService;
import ai.lexmind.common.security.UserPrincipal;
import ai.lexmind.intelligence.api.dto.DashboardDtos.ArgumentDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.ArgumentsView;
import ai.lexmind.intelligence.api.dto.DashboardDtos.FactCounts;
import ai.lexmind.intelligence.api.dto.DashboardDtos.FactDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.IracDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.IssueDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.OverviewDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.PartyDto;
import ai.lexmind.intelligence.api.dto.DashboardDtos.TimelineEventDto;
import ai.lexmind.intelligence.domain.CaseFact;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.ArgumentRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.CaseFactRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.IracAnalysisRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.LegalIssueRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.TimelineEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Read-side queries that power the Case Analysis Dashboard tabs. All are RBAC-guarded. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final CaseAccessService accessService;
    private final CaseFactRepository factRepository;
    private final TimelineEventRepository timelineRepository;
    private final LegalIssueRepository issueRepository;
    private final ArgumentRepository argumentRepository;
    private final IracAnalysisRepository iracRepository;
    private final AnalysisIngestService analysisIngestService;

    public DashboardService(CaseAccessService accessService, CaseFactRepository factRepository,
                            TimelineEventRepository timelineRepository, LegalIssueRepository issueRepository,
                            ArgumentRepository argumentRepository, IracAnalysisRepository iracRepository,
                            AnalysisIngestService analysisIngestService) {
        this.accessService = accessService;
        this.factRepository = factRepository;
        this.timelineRepository = timelineRepository;
        this.issueRepository = issueRepository;
        this.argumentRepository = argumentRepository;
        this.iracRepository = iracRepository;
        this.analysisIngestService = analysisIngestService;
    }

    public OverviewDto overview(UUID caseId, UserPrincipal user) {
        CaseFile c = accessService.getAccessibleCase(caseId, user);
        List<CaseFact> facts = factRepository.findByCaseId(caseId);
        long established = facts.stream().filter(f -> "ESTABLISHED".equals(f.getFactStatus())).count();
        long disputed = facts.stream().filter(f -> "DISPUTED".equals(f.getFactStatus())).count();
        long missing = facts.stream().filter(f -> "MISSING".equals(f.getFactStatus())).count();
        FactCounts counts = new FactCounts(established, disputed, missing, facts.size());

        List<PartyDto> parties = c.getParties().stream()
                .map(p -> new PartyDto(p.getName(), p.getSide(), p.getCounsel()))
                .toList();

        String runStatus = null;
        var latestCompleted = analysisIngestService.latestForCase(caseId).orElse(null);
        java.time.Instant completedAt = null;
        if (latestCompleted != null) {
            runStatus = latestCompleted.getStatus();
            completedAt = latestCompleted.getCompletedAt();
        }

        return OverviewDto.of(c, parties, counts,
                issueRepository.findByCaseIdOrderByRankAsc(caseId).size(),
                argumentRepository.findByCaseId(caseId).size(),
                timelineRepository.findByCaseIdOrderBySortOrderAsc(caseId).size(),
                runStatus, completedAt);
    }

    public List<FactDto> facts(UUID caseId, UserPrincipal user, String status) {
        accessService.getAccessibleCase(caseId, user);
        List<CaseFact> facts = (status == null || status.isBlank())
                ? factRepository.findByCaseId(caseId)
                : factRepository.findByCaseIdAndFactStatus(caseId, status.toUpperCase());
        return facts.stream().map(FactDto::from).toList();
    }

    public List<TimelineEventDto> timeline(UUID caseId, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);
        return timelineRepository.findByCaseIdOrderBySortOrderAsc(caseId).stream()
                .map(TimelineEventDto::from).toList();
    }

    public List<IssueDto> issues(UUID caseId, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);
        return issueRepository.findByCaseIdOrderByRankAsc(caseId).stream()
                .map(IssueDto::from).toList();
    }

    public ArgumentsView arguments(UUID caseId, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);
        List<ArgumentDto> petitioner = argumentRepository
                .findByCaseIdAndPartySide(caseId, "PETITIONER").stream().map(ArgumentDto::from).toList();
        List<ArgumentDto> respondent = argumentRepository
                .findByCaseIdAndPartySide(caseId, "RESPONDENT").stream().map(ArgumentDto::from).toList();
        return new ArgumentsView(petitioner, respondent);
    }

    public List<IracDto> irac(UUID caseId, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);
        return iracRepository.findByCaseId(caseId).stream().map(IracDto::from).toList();
    }
}
