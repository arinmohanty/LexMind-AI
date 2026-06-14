import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/app/config/queryKeys";
import { analysisApi } from "@/features/analysis/api";
import type { RunStatus } from "@/types/api";

const TERMINAL: RunStatus[] = ["COMPLETED", "FAILED", "PARTIAL"];

export function useStartAnalysis(caseId: string) {
  return useMutation({ mutationFn: () => analysisApi.start(caseId) });
}

/**
 * Polls a run until it reaches a terminal state, then invalidates the case dashboard so the
 * tabs refresh (progressive-fill pattern, Phase 3 component hierarchy §6).
 */
export function useAnalysisRun(runId: string | null, caseId?: string) {
  const qc = useQueryClient();
  return useQuery({
    queryKey: queryKeys.analysisRun(runId ?? "none"),
    queryFn: async () => {
      const run = await analysisApi.getRun(runId as string);
      if (TERMINAL.includes(run.status) && caseId) {
        qc.invalidateQueries({ queryKey: ["dashboard", caseId] });
        qc.invalidateQueries({ queryKey: queryKeys.irac(caseId) });
      }
      return run;
    },
    enabled: !!runId,
    refetchInterval: (query) => {
      const status = query.state.data?.status;
      return status && TERMINAL.includes(status) ? false : 2500;
    },
  });
}
