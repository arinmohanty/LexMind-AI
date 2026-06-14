package ai.lexmind.analytics.service;

import ai.lexmind.analytics.api.dto.AnalyticsDtos.CaseAnalyticsDto;
import ai.lexmind.analytics.api.dto.AnalyticsDtos.CaseStrengthDto;
import ai.lexmind.analytics.api.dto.AnalyticsDtos.MatterReadinessDto;
import ai.lexmind.analytics.api.dto.AnalyticsDtos.PortfolioDto;
import ai.lexmind.analytics.api.dto.AnalyticsDtos.ReadinessDto;
import ai.lexmind.analytics.api.dto.AnalyticsDtos.RiskDto;
import ai.lexmind.analytics.domain.CaseStrength;
import ai.lexmind.analytics.domain.ReadinessScore;
import ai.lexmind.analytics.domain.RiskAssessment;
import ai.lexmind.analytics.repo.AnalyticsRepositories.CaseStrengthRepository;
import ai.lexmind.analytics.repo.AnalyticsRepositories.ReadinessScoreRepository;
import ai.lexmind.analytics.repo.AnalyticsRepositories.RiskAssessmentRepository;
import ai.lexmind.casefile.domain.CaseFile;
import ai.lexmind.casefile.repo.CaseRepository;
import ai.lexmind.casefile.service.CaseAccessService;
import ai.lexmind.common.security.UserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final int PORTFOLIO_LIMIT = 50;

    private final CaseAccessService accessService;
    private final CaseRepository caseRepository;
    private final CaseStrengthRepository strengthRepository;
    private final RiskAssessmentRepository riskRepository;
    private final ReadinessScoreRepository readinessRepository;
    private final ObjectMapper objectMapper;

    public AnalyticsService(CaseAccessService accessService, CaseRepository caseRepository,
                            CaseStrengthRepository strengthRepository,
                            RiskAssessmentRepository riskRepository,
                            ReadinessScoreRepository readinessRepository, ObjectMapper objectMapper) {
        this.accessService = accessService;
        this.caseRepository = caseRepository;
        this.strengthRepository = strengthRepository;
        this.riskRepository = riskRepository;
        this.readinessRepository = readinessRepository;
        this.objectMapper = objectMapper;
    }

    public CaseAnalyticsDto getCaseAnalytics(UUID caseId, UserPrincipal user) {
        accessService.getAccessibleCase(caseId, user);
        CaseStrengthDto strength = strengthRepository.findFirstByCaseIdOrderByCreatedAtDesc(caseId)
                .map(this::toStrengthDto).orElse(null);
        List<RiskDto> risks = riskRepository.findByCaseId(caseId).stream().map(RiskDto::from).toList();
        ReadinessDto readiness = readinessRepository.findFirstByCaseIdOrderByComputedAtDesc(caseId)
                .map(ReadinessDto::from).orElse(null);
        return new CaseAnalyticsDto(strength, risks, readiness);
    }

    public PortfolioDto getPortfolio(UserPrincipal user) {
        List<CaseFile> cases = scopedCases(user);
        List<MatterReadinessDto> matters = new java.util.ArrayList<>();
        Map<String, Long> riskByType = new HashMap<>();
        double readinessSum = 0;
        int readinessCount = 0;
        long highRiskCases = 0;

        for (CaseFile c : cases) {
            Double overall = readinessRepository.findFirstByCaseIdOrderByComputedAtDesc(c.getId())
                    .map(r -> r.getOverallReadiness() == null ? null : r.getOverallReadiness().doubleValue())
                    .orElse(null);
            List<RiskAssessment> risks = riskRepository.findByCaseId(c.getId());
            long highRisks = risks.stream().filter(r -> "HIGH".equals(r.getSeverity())).count();
            risks.forEach(r -> riskByType.merge(r.getRiskType(), 1L, Long::sum));
            if (highRisks > 0) {
                highRiskCases++;
            }
            if (overall != null) {
                readinessSum += overall;
                readinessCount++;
            }
            matters.add(new MatterReadinessDto(c.getId(), c.getTitle(), overall, highRisks));
        }

        Double avgReadiness = readinessCount == 0 ? null : readinessSum / readinessCount;
        return new PortfolioDto(matters.size(), avgReadiness, highRiskCases, matters, riskByType);
    }

    // ---- helpers ----

    private List<CaseFile> scopedCases(UserPrincipal user) {
        PageRequest page = PageRequest.of(0, PORTFOLIO_LIMIT);
        if ("SUPER_ADMIN".equals(user.getRole())) {
            return caseRepository.findAll(page).getContent();
        }
        if ("LAW_FIRM_ADMIN".equals(user.getRole()) && user.getOrganizationId() != null) {
            return caseRepository.findByOrganizationIdAndStatusNot(
                    user.getOrganizationId(), "DELETED", page).getContent();
        }
        return caseRepository.findByOwnerIdAndStatusNot(user.getId(), "DELETED", page).getContent();
    }

    private CaseStrengthDto toStrengthDto(CaseStrength cs) {
        Map<String, List<String>> findings = parseFindings(cs.getFindingsJson());
        Double score = cs.getOverallScore() == null ? null : cs.getOverallScore().doubleValue();
        return new CaseStrengthDto(
                score,
                findings.getOrDefault("strong", List.of()),
                findings.getOrDefault("weak", List.of()),
                findings.getOrDefault("missingEvidence", List.of()),
                findings.getOrDefault("openQuestions", List.of()));
    }

    private Map<String, List<String>> parseFindings(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }
}
