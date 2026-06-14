import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "@/app/config/queryKeys";
import { casesApi, type CreateCasePayload } from "@/features/cases/api";

export function useCases(page = 0) {
  return useQuery({
    queryKey: queryKeys.cases(page),
    queryFn: () => casesApi.list(page),
  });
}

export function useCase(caseId: string) {
  return useQuery({
    queryKey: queryKeys.case(caseId),
    queryFn: () => casesApi.get(caseId),
    enabled: !!caseId,
  });
}

export function useCreateCase() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateCasePayload) => casesApi.create(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ["cases"] }),
  });
}

export function useCaseDocuments(caseId: string, refetchInterval?: number) {
  return useQuery({
    queryKey: queryKeys.documents(caseId),
    queryFn: () => casesApi.documents(caseId),
    enabled: !!caseId,
    refetchInterval,
  });
}

export function useUploadDocument(caseId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (file: File) => casesApi.upload(caseId, file),
    onSuccess: () => qc.invalidateQueries({ queryKey: queryKeys.documents(caseId) }),
  });
}
