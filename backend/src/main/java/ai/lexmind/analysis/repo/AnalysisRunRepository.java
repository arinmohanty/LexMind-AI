package ai.lexmind.analysis.repo;

import ai.lexmind.analysis.domain.AnalysisRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, UUID> {
    Optional<AnalysisRun> findFirstByCaseIdOrderByCreatedAtDesc(UUID caseId);
}
