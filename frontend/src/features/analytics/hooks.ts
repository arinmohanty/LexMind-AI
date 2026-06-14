import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/app/config/queryKeys";
import { analyticsApi } from "@/features/analytics/api";

export function useCaseAnalytics(caseId: string) {
  return useQuery({
    queryKey: queryKeys.caseAnalytics(caseId),
    queryFn: () => analyticsApi.caseAnalytics(caseId),
    enabled: !!caseId,
  });
}

export function usePortfolio() {
  return useQuery({
    queryKey: queryKeys.portfolio(),
    queryFn: () => analyticsApi.portfolio(),
  });
}
