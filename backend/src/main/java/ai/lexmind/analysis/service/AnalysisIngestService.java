package ai.lexmind.analysis.service;

import ai.lexmind.analysis.client.AiContracts.AgentResultsPayload;
import ai.lexmind.analysis.domain.AgentExecution;
import ai.lexmind.analysis.domain.AnalysisRun;
import ai.lexmind.analysis.repo.AgentExecutionRepository;
import ai.lexmind.analysis.repo.AnalysisRunRepository;
import ai.lexmind.common.error.AppExceptions.NotFoundException;
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

    public AnalysisIngestService(AnalysisRunRepository runRepository,
                                 AgentExecutionRepository agentExecutionRepository,
                                 CaseFactRepository factRepository,
                                 TimelineEventRepository timelineRepository,
                                 LegalIssueRepository issueRepository,
                                 ArgumentRepository argumentRepository,
                                 IracAnalysisRepository iracRepository) {
        this.runRepository = runRepository;
        this.agentExecutionRepository = agentExecutionRepository;
        this.factRepository = factRepository;
        this.timelineRepository = timelineRepository;
        this.issueRepository = issueRepository;
        this.argumentRepository = argumentRepository;
        this.iracRepository = iracRepository;
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

        // Replace prior intelligence for this case.
        factRepository.deleteByCaseId(caseId);
        timelineRepository.deleteByCaseId(caseId);
        issueRepository.deleteByCaseId(caseId);
        argumentRepository.deleteByCaseId(caseId);
        iracRepository.deleteByCaseId(caseId);

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
}
