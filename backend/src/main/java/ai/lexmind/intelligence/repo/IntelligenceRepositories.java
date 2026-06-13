package ai.lexmind.intelligence.repo;

import ai.lexmind.intelligence.domain.Argument;
import ai.lexmind.intelligence.domain.CaseFact;
import ai.lexmind.intelligence.domain.IracAnalysis;
import ai.lexmind.intelligence.domain.LegalIssue;
import ai.lexmind.intelligence.domain.TimelineEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Read-side repositories for the legal-intelligence tables (grouped for cohesion). */
public final class IntelligenceRepositories {

    private IntelligenceRepositories() {}

    public interface CaseFactRepository extends JpaRepository<CaseFact, UUID> {
        List<CaseFact> findByCaseId(UUID caseId);
        List<CaseFact> findByCaseIdAndFactStatus(UUID caseId, String factStatus);
        void deleteByCaseId(UUID caseId);
    }

    public interface TimelineEventRepository extends JpaRepository<TimelineEvent, UUID> {
        List<TimelineEvent> findByCaseIdOrderBySortOrderAsc(UUID caseId);
        void deleteByCaseId(UUID caseId);
    }

    public interface LegalIssueRepository extends JpaRepository<LegalIssue, UUID> {
        List<LegalIssue> findByCaseIdOrderByRankAsc(UUID caseId);
        void deleteByCaseId(UUID caseId);
    }

    public interface ArgumentRepository extends JpaRepository<Argument, UUID> {
        List<Argument> findByCaseId(UUID caseId);
        List<Argument> findByCaseIdAndPartySide(UUID caseId, String partySide);
        void deleteByCaseId(UUID caseId);
    }

    public interface IracAnalysisRepository extends JpaRepository<IracAnalysis, UUID> {
        List<IracAnalysis> findByCaseId(UUID caseId);
        void deleteByCaseId(UUID caseId);
    }
}
