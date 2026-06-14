import { apiDelete, apiGet, apiPost, apiPostForm } from "@/lib/apiClient";
import type { CaseDetail, CaseParty, CaseSummary, DocumentDto, PageResponse } from "@/types/api";

export interface CreateCasePayload {
  title: string;
  caseNumber?: string;
  court?: string;
  jurisdiction?: string;
  caseType?: string;
  stage?: string;
  filingDate?: string;
  parties?: CaseParty[];
}

export const casesApi = {
  list: (page = 0, size = 20) =>
    apiGet<PageResponse<CaseSummary>>(`/api/v1/cases?page=${page}&size=${size}`),
  get: (id: string) => apiGet<CaseDetail>(`/api/v1/cases/${id}`),
  create: (body: CreateCasePayload) => apiPost<CaseDetail>("/api/v1/cases", body),
  archive: (id: string) => apiDelete<void>(`/api/v1/cases/${id}`),
  documents: (id: string) => apiGet<DocumentDto[]>(`/api/v1/cases/${id}/documents`),
  upload: (id: string, file: File) => {
    const form = new FormData();
    form.append("file", file);
    return apiPostForm<DocumentDto>(`/api/v1/cases/${id}/documents`, form);
  },
};
