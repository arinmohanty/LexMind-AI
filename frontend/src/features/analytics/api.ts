import { apiGet } from "@/lib/apiClient";
import type { CaseAnalytics, Portfolio } from "@/types/api";

export const analyticsApi = {
  caseAnalytics: (caseId: string) =>
    apiGet<CaseAnalytics>(`/api/v1/cases/${caseId}/analytics`),
  portfolio: () => apiGet<Portfolio>("/api/v1/analytics/portfolio"),
};
