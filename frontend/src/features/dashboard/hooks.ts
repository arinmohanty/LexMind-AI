import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "@/app/config/queryKeys";
import { dashboardApi } from "@/features/dashboard/api";

export function useOverview(caseId: string) {
  return useQuery({
    queryKey: queryKeys.dashboard(caseId, "overview"),
    queryFn: () => dashboardApi.overview(caseId),
    enabled: !!caseId,
  });
}

export function useFacts(caseId: string, status?: string) {
  return useQuery({
    queryKey: [...queryKeys.dashboard(caseId, "facts"), status ?? "all"],
    queryFn: () => dashboardApi.facts(caseId, status),
    enabled: !!caseId,
  });
}

export function useTimeline(caseId: string) {
  return useQuery({
    queryKey: queryKeys.dashboard(caseId, "timeline"),
    queryFn: () => dashboardApi.timeline(caseId),
    enabled: !!caseId,
  });
}

export function useIssues(caseId: string) {
  return useQuery({
    queryKey: queryKeys.dashboard(caseId, "issues"),
    queryFn: () => dashboardApi.issues(caseId),
    enabled: !!caseId,
  });
}

export function useArguments(caseId: string) {
  return useQuery({
    queryKey: queryKeys.dashboard(caseId, "arguments"),
    queryFn: () => dashboardApi.arguments(caseId),
    enabled: !!caseId,
  });
}

export function useIrac(caseId: string) {
  return useQuery({
    queryKey: queryKeys.irac(caseId),
    queryFn: () => dashboardApi.irac(caseId),
    enabled: !!caseId,
  });
}
