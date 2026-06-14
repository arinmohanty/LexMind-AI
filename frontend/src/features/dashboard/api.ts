import { apiGet } from "@/lib/apiClient";
import type {
  ArgumentsView,
  DashboardOverview,
  Fact,
  Irac,
  LegalIssue,
  TimelineEvent,
} from "@/types/api";

const base = (caseId: string) => `/api/v1/cases/${caseId}`;

export const dashboardApi = {
  overview: (caseId: string) => apiGet<DashboardOverview>(`${base(caseId)}/dashboard/overview`),
  facts: (caseId: string, status?: string) =>
    apiGet<Fact[]>(`${base(caseId)}/dashboard/facts${status ? `?status=${status}` : ""}`),
  timeline: (caseId: string) => apiGet<TimelineEvent[]>(`${base(caseId)}/dashboard/timeline`),
  issues: (caseId: string) => apiGet<LegalIssue[]>(`${base(caseId)}/dashboard/issues`),
  arguments: (caseId: string) => apiGet<ArgumentsView>(`${base(caseId)}/dashboard/arguments`),
  irac: (caseId: string) => apiGet<Irac[]>(`${base(caseId)}/irac`),
};
