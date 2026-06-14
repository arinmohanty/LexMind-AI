package ai.lexmind.analytics.api.dto;

import ai.lexmind.analytics.domain.ReadinessScore;
import ai.lexmind.analytics.domain.RiskAssessment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AnalyticsDtos {

    private AnalyticsDtos() {}

    private static Double toD(BigDecimal v) {
        return v == null ? null : v.doubleValue();
    }

    public record RiskDto(UUID id, String riskType, String severity, String description) {
        public static RiskDto from(RiskAssessment r) {
            return new RiskDto(r.getId(), r.getRiskType(), r.getSeverity(), r.getDescription());
        }
    }

    public record CaseStrengthDto(
            Double overallScore,
            List<String> strong,
            List<String> weak,
            List<String> missingEvidence,
            List<String> openQuestions
    ) {}

    public record ReadinessDto(
            Double evidenceReadiness,
            Double witnessReadiness,
            Double researchReadiness,
            Double hearingReadiness,
            Double overallReadiness
    ) {
        public static ReadinessDto from(ReadinessScore r) {
            return new ReadinessDto(toD(r.getEvidenceReadiness()), toD(r.getWitnessReadiness()),
                    toD(r.getResearchReadiness()), toD(r.getHearingReadiness()),
                    toD(r.getOverallReadiness()));
        }
    }

    public record CaseAnalyticsDto(
            CaseStrengthDto strength,
            List<RiskDto> risks,
            ReadinessDto readiness
    ) {}

    // ---- portfolio ----

    public record MatterReadinessDto(UUID caseId, String title, Double overallReadiness, long highRisks) {}

    public record PortfolioDto(
            int caseCount,
            Double avgReadiness,
            long highRiskCases,
            List<MatterReadinessDto> matters,
            Map<String, Long> riskByType
    ) {}
}
