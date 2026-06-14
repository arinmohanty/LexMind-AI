import { apiGet, apiPost } from "@/lib/apiClient";
import type { AnalysisRun } from "@/types/api";

export const analysisApi = {
  start: (caseId: string) => apiPost<AnalysisRun>(`/api/v1/cases/${caseId}/analyze`),
  getRun: (runId: string) => apiGet<AnalysisRun>(`/api/v1/analysis/${runId}`),
};
