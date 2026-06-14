package ai.lexmind.analysis.service;

import ai.lexmind.analysis.client.AiContracts.AgentResultsPayload;
import ai.lexmind.analysis.domain.AgentExecution;
import ai.lexmind.analysis.domain.AnalysisRun;
import ai.lexmind.analysis.repo.AgentExecutionRepository;
import ai.lexmind.analysis.repo.AnalysisRunRepository;
import ai.lexmind.analytics.domain.CaseStrength;
import ai.lexmind.analytics.domain.ReadinessScore;
import ai.lexmind.analytics.domain.RiskAssessment;
import ai.lexmind.analytics.repo.AnalyticsRepositories.CaseStrengthRepository;
import ai.lexmind.analytics.repo.AnalyticsRepositories.ReadinessScoreRepository;
import ai.lexmind.analytics.repo.AnalyticsRepositories.RiskAssessmentRepository;
import ai.lexmind.common.error.AppExceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.lexmind.intelligence.domain.Argument;
import ai.lexmind.intelligence.domain.CaseFact;
import ai.lexmind.intelligence.domain.IracAnalysis;
import ai.lexmind.intelligence.domain.LegalIssue;
import ai.lexmind.intelligence.domain.TimelineEvent;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.ArgumentRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.CaseFactRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.IracAnalysisRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.LegalIssueRepository;
import ai.lexmind.intelligence.repo.IntelligenceRepositories.TimelineEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persists structured AI results into the normalized intelligence tables and updates run
 * status. Re-running replaces a case's prior intelligence so the dashboard reflects the
 * latest analysis (idempotent per case).
 */
@Service
public class AnalysisIngestService {

    private final AnalysisRunRepository runRepository;
    private final AgentExecutionRepository agentExecutionRepository;
    private final CaseFactRepository factRepository;
    private final TimelineEventRepository timelineRepository;
    private final LegalIssueRepository issueRepository;
    private final ArgumentRepository argumentRepository;
    private final IracAnalysisRepository iracRepository;
    private final CaseStrengthRepository strengthRepository;
    private final RiskAssessmentRepository riskRepository;
    private final ReadinessScoreRepository readinessRepository;
    private final ObjectMapper objectMapper;

    public AnalysisIngestService(AnalysisRunRepository runRepository,
                                 AgentExecutionRepository agentExecutionRepository,
                                 CaseFactRepository factRepository,
                                 TimelineEventRepository timelineRepository,
                                 LegalIssueRepository issueRepository,
                                 ArgumentRepository argumentRepository,
                                 IracAnalysisRepository iracRepository,
                                 CaseStrengthRepository strengthRepository,
                                 RiskAssessmentRepository riskRepository,
                                 ReadinessScoreRepository readinessRepository,
                                 ObjectMapper objectMapper) {
        this.runRepository = runRepository;
        this.agentExecutionRepository = agentExecutionRepository;
        this.factRepository = factRepository;
        this.timelineRepository = timelineRepository;
        this.issueRepository = issueRepository;
        this.argumentRepository = argumentRepository;
        this.iracRepository = iracRepository;
        this.strengthRepository = strengthRepository;
        this.riskRepository = riskRepository;
        this.readinessRepository = readinessRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void markProcessing(UUID runId) {
        AnalysisRun run = requireRun(runId);
        run.setStatus("PROCESSING");
        run.setStartedAt(Instant.now());
    }

    @Transactional
    public void markFailed(UUID runId, String message) {
        AnalysisRun run = requireRun(runId);
        run.setStatus("FAILED");
        run.setErrorMessage(truncate(message, 1000));
        run.setCompletedAt(Instant.now());
    }

    @Transactional
    public void ingest(UUID runId, AgentResultsPayload p) {
        AnalysisRun run = requireRun(runId);
        UUID caseId = run.getCaseId();

        // Replace prior intelligence + analytics for this case.
        factRepository.deleteByCaseId(caseId);
        timelineRepository.deleteByCaseId(caseId);
        issueRepository.deleteByCaseId(caseId);
        argumentRepository.deleteByCaseId(caseId);
        iracRepository.deleteByCaseId(caseId);
        strengthRepository.deleteByCaseId(caseId);
        riskRepository.deleteByCaseId(caseId);
        readinessRepository.deleteByCaseId(caseId);

        if (p.facts() != null) {
            factRepository.saveAll(p.facts().stream().map(f -> {
                CaseFact e = new CaseFact();
                e.setCaseId(caseId);
                e.setAnalysisRunId(runId);
                e.setFactText(f.factText());
                e.setFactStatus(defaultStr(f.factStatus(), "ESTABLISHED"));
                e.setSourceExcerpt(f.sourceExcerpt());
                e.setConfidence(f.confidence());
                return e;
            }).toList());
        }

        if (p.timeline() != null) {
            int[] order = {0};
            timelineRepository.saveAll(p.timeline().stream().map(t -> {
                TimelineEvent e = new TimelineEvent();
                e.setCaseId(caseId);
                e.setEventDate(t.eventDate());
                e.setEventText(t.eventText());
                e.setEventType(t.eventType());
                e.setSortOrder(t.sortOrder() != null ? t.sortOrder() : order[0]++);
                return e;
            }).toList());
        }

        if (p.issues() != null) {
            int[] rank = {1};
            issueRepository.saveAll(p.issues().stream().map(i -> {
                LegalIssue e = new LegalIssue();
                e.setCaseId(caseId);
                e.setAnalysisRunId(runId);
                e.setIssueText(i.issueText());
                e.setIssueType(defaultStr(i.issueType(), "PRIMARY"));
                e.setRank(i.rank() != null ? i.rank() : rank[0]++);
                e.setImportanceScore(i.importanceScore());
                return e;
            }).toList());
        }

        if (p.arguments() != null) {
            argumentRepository.saveAll(p.arguments().stream().map(a -> {
                Argument e = new Argument();
                e.setCaseId(caseId);
                e.setPartySide(a.partySide());
                e.setArgumentText(a.argumentText());
                e.setStrength(a.strength());
                e.setSourceExcerpt(a.sourceExcerpt());
                return e;
            }).toList());
        }

        if (p.irac() != null) {
            iracRepository.saveAll(p.irac().stream().map(ir -> {
                IracAnalysis e = new IracAnalysis();
                e.setCaseId(caseId);
                e.setIssue(ir.issue());
                e.setRule(ir.rule());
                e.setApplication(ir.application());
                e.setConclusion(ir.conclusion());
                return e;
            }).toList());
        }

        // ---- analytics (case strength / risk / readiness) ----
        if (p.caseStrength() != null) {
            CaseStrength cs = new CaseStrength();
            cs.setCaseId(caseId);
            cs.setAnalysisRunId(runId);
            cs.setOverallScore(toBigDecimal(p.caseStrength().overallScore()));
            cs.setFindingsJson(writeJson(Map.of(
                    "strong", nullToEmpty(p.caseStrength().strong()),
                    "weak", nullToEmpty(p.caseStrength().weak()),
                    "missingEvidence", nullToEmpty(p.caseStrength().missingEvidence()),
                    "openQuestions", nullToEmpty(p.caseStrength().openQuestions()))));
            strengthRepository.save(cs);
        }
        if (p.risks() != null) {
            riskRepository.saveAll(p.risks().stream().map(r -> {
                RiskAssessment ra = new RiskAssessment();
                ra.setCaseId(caseId);
                ra.setRiskType(defaultStr(r.riskType(), "DOCUMENTATION"));
                ra.setSeverity(r.severity());
                ra.setDescription(r.description() == null ? "" : r.description());
                return ra;
            }).toList());
        }
        if (p.readiness() != null) {
            ReadinessScore rs = new ReadinessScore();
            rs.setCaseId(caseId);
            rs.setEvidenceReadiness(toBigDecimal(p.readiness().evidenceReadiness()));
            rs.setWitnessReadiness(toBigDecimal(p.readiness().witnessReadiness()));
            rs.setResearchReadiness(toBigDecimal(p.readiness().researchReadiness()));
            rs.setHearingReadiness(toBigDecimal(p.readiness().hearingReadiness()));
            rs.setOverallReadiness(toBigDecimal(p.readiness().overallReadiness()));
            readinessRepository.save(rs);
        }

        if (p.agentExecutions() != null) {
            agentExecutionRepository.saveAll(p.agentExecutions().stream().map(ae -> {
                AgentExecution e = new AgentExecution();
                e.setAnalysisRunId(runId);
                e.setAgentType(ae.agentType());
                e.setStatus(defaultStr(ae.status(), "COMPLETED"));
                e.setLatencyMs(ae.latencyMs());
                e.setTokens(ae.tokens() != null ? ae.tokens() : 0L);
                e.setModel(ae.model());
                e.setOutputJson(ae.outputJson());
                e.setErrorMessage(ae.errorMessage());
                return e;
            }).toList());
        }

        boolean anyAgentFailed = p.agentExecutions() != null
                && p.agentExecutions().stream().anyMatch(a -> "FAILED".equals(a.status()));
        run.setStatus(anyAgentFailed ? "PARTIAL" : "COMPLETED");
        run.setModel(p.model());
        run.setTotalTokens(p.totalTokens() != null ? p.totalTokens() : 0L);
        run.setCostUsd(p.costUsd() != null ? p.costUsd() : BigDecimal.ZERO);
        run.setCompletedAt(Instant.now());
    }

    public List<AgentExecution> agentsForRun(UUID runId) {
        return agentExecutionRepository.findByAnalysisRunId(runId);
    }

    public Optional<AnalysisRun> latestForCase(UUID caseId) {
        return runRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId);
    }

    private AnalysisRun requireRun(UUID runId) {
        return runRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("RUN_NOT_FOUND", "Analysis run not found"));
    }

    private static String defaultStr(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    private static String truncate(String v, int max) {
        if (v == null) return null;
        return v.length() <= max ? v : v.substring(0, max);
    }

    private static BigDecimal toBigDecimal(Double d) {
        return d == null ? null : BigDecimal.valueOf(d);
    }

    private static List<String> nullToEmpty(List<String> list) {
        return list == null ? List.of() : list;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }
}
