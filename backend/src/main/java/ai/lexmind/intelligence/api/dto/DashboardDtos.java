package ai.lexmind.intelligence.api.dto;

import ai.lexmind.casefile.domain.CaseFile;
import ai.lexmind.intelligence.domain.Argument;
import ai.lexmind.intelligence.domain.CaseFact;
import ai.lexmind.intelligence.domain.IracAnalysis;
import ai.lexmind.intelligence.domain.LegalIssue;
import ai.lexmind.intelligence.domain.TimelineEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class DashboardDtos {

    private DashboardDtos() {}

    public record FactDto(UUID id, String factText, String factStatus, String sourceExcerpt,
                          BigDecimal confidence) {
        public static FactDto from(CaseFact f) {
            return new FactDto(f.getId(), f.getFactText(), f.getFactStatus(),
                    f.getSourceExcerpt(), f.getConfidence());
        }
    }

    public record TimelineEventDto(UUID id, LocalDate eventDate, String eventText,
                                   String eventType, int sortOrder) {
        public static TimelineEventDto from(TimelineEvent t) {
            return new TimelineEventDto(t.getId(), t.getEventDate(), t.getEventText(),
                    t.getEventType(), t.getSortOrder());
        }
    }

    public record IssueDto(UUID id, String issueText, String issueType, int rank,
                           BigDecimal importanceScore) {
        public static IssueDto from(LegalIssue i) {
            return new IssueDto(i.getId(), i.getIssueText(), i.getIssueType(),
                    i.getRank(), i.getImportanceScore());
        }
    }

    public record ArgumentDto(UUID id, String partySide, String argumentText, String strength,
                              String sourceExcerpt) {
        public static ArgumentDto from(Argument a) {
            return new ArgumentDto(a.getId(), a.getPartySide(), a.getArgumentText(),
                    a.getStrength(), a.getSourceExcerpt());
        }
    }

    /** Side-by-side argument view (wireframe ArgumentSplit). */
    public record ArgumentsView(List<ArgumentDto> petitioner, List<ArgumentDto> respondent) {}

    public record IracDto(UUID id, String issue, String rule, String application, String conclusion) {
        public static IracDto from(IracAnalysis i) {
            return new IracDto(i.getId(), i.getIssue(), i.getRule(),
                    i.getApplication(), i.getConclusion());
        }
    }

    public record PartyDto(String name, String side, String counsel) {}

    public record FactCounts(long established, long disputed, long missing, long total) {}

    public record OverviewDto(
            UUID caseId, String title, String caseNumber, String court, String jurisdiction,
            String caseType, String stage, LocalDate filingDate, String status,
            List<PartyDto> parties,
            FactCounts factCounts, long issueCount, long argumentCount, long timelineCount,
            String latestRunStatus, Instant latestRunCompletedAt
    ) {
        public static OverviewDto of(CaseFile c, List<PartyDto> parties, FactCounts factCounts,
                                     long issueCount, long argumentCount, long timelineCount,
                                     String latestRunStatus, Instant latestRunCompletedAt) {
            return new OverviewDto(c.getId(), c.getTitle(), c.getCaseNumber(), c.getCourt(),
                    c.getJurisdiction(), c.getCaseType(), c.getStage(), c.getFilingDate(),
                    c.getStatus(), parties, factCounts, issueCount, argumentCount, timelineCount,
                    latestRunStatus, latestRunCompletedAt);
        }
    }
}
