/** Centralized React Query keys (Phase 3 / component hierarchy §6). */
export const queryKeys = {
  me: ["me"] as const,
  cases: (page?: number) => ["cases", page ?? 0] as const,
  case: (caseId: string) => ["cases", caseId] as const,
  documents: (caseId: string) => ["documents", caseId] as const,
  analysisRun: (runId: string) => ["analysis", "run", runId] as const,
  dashboard: (caseId: string, section: string) => ["dashboard", caseId, section] as const,
  irac: (caseId: string) => ["irac", caseId] as const,
};
