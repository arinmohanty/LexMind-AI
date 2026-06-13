package ai.lexmind.analysis.repo;

import ai.lexmind.analysis.domain.AgentExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AgentExecutionRepository extends JpaRepository<AgentExecution, UUID> {
    List<AgentExecution> findByAnalysisRunId(UUID analysisRunId);
}
