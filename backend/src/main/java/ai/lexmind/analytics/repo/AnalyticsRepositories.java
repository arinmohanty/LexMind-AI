package ai.lexmind.analytics.repo;

import ai.lexmind.analytics.domain.CaseStrength;
import ai.lexmind.analytics.domain.ReadinessScore;
import ai.lexmind.analytics.domain.RiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Read/write repositories for the analytics tables (grouped for cohesion). */
public final class AnalyticsRepositories {

    private AnalyticsRepositories() {}

    public interface CaseStrengthRepository extends JpaRepository<CaseStrength, UUID> {
        Optional<CaseStrength> findFirstByCaseIdOrderByCreatedAtDesc(UUID caseId);
        void deleteByCaseId(UUID caseId);
    }

    public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
        List<RiskAssessment> findByCaseId(UUID caseId);
        long countByCaseIdAndSeverity(UUID caseId, String severity);
        void deleteByCaseId(UUID caseId);
    }

    public interface ReadinessScoreRepository extends JpaRepository<ReadinessScore, UUID> {
        Optional<ReadinessScore> findFirstByCaseIdOrderByComputedAtDesc(UUID caseId);
        void deleteByCaseId(UUID caseId);
    }
}
