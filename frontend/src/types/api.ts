// Shared API types — mirror the backend DTOs (Phase 4). In a later iteration these can be
// generated from the OpenAPI schema for guaranteed parity.

export interface ApiError {
  code: string;
  message: string;
  details?: { field: string; message: string }[];
}

export interface ApiResponse<T> {
  data: T;
  error: ApiError | null;
  traceId: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export type Role =
  | "SUPER_ADMIN"
  | "LAW_FIRM_ADMIN"
  | "ADVOCATE"
  | "RESEARCHER"
  | "LAW_STUDENT";

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: Role;
  organizationId: string | null;
  status: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
}

export interface CaseParty {
  id?: string;
  name: string;
  side: string;
  counsel?: string | null;
}

export interface CaseSummary {
  id: string;
  title: string;
  caseNumber: string | null;
  court: string | null;
  caseType: string | null;
  stage: string | null;
  status: string;
  updatedAt: string;
}

export interface CaseDetail {
  id: string;
  ownerId: string;
  organizationId: string | null;
  title: string;
  caseNumber: string | null;
  court: string | null;
  jurisdiction: string | null;
  caseType: string | null;
  stage: string | null;
  filingDate: string | null;
  status: string;
  parties: CaseParty[];
  createdAt: string;
  updatedAt: string;
}

export interface DocumentDto {
  id: string;
  caseId: string;
  originalFilename: string;
  mimeType: string;
  sizeBytes: number;
  docType: string;
  status: "QUEUED" | "PROCESSING" | "DONE" | "FAILED";
  pageCount: number;
  ocrApplied: boolean;
  errorMessage: string | null;
  createdAt: string;
}

export interface AgentExecution {
  agentType: string;
  status: string;
  latencyMs: number | null;
  tokens: number;
}

export type RunStatus = "QUEUED" | "PROCESSING" | "COMPLETED" | "FAILED" | "PARTIAL";

export interface AnalysisRun {
  id: string;
  caseId: string;
  status: RunStatus;
  model: string | null;
  totalTokens: number;
  costUsd: number;
  errorMessage: string | null;
  startedAt: string | null;
  completedAt: string | null;
  createdAt: string;
  agents: AgentExecution[];
}

export interface FactCounts {
  established: number;
  disputed: number;
  missing: number;
  total: number;
}

export interface DashboardOverview {
  caseId: string;
  title: string;
  caseNumber: string | null;
  court: string | null;
  jurisdiction: string | null;
  caseType: string | null;
  stage: string | null;
  filingDate: string | null;
  status: string;
  parties: { name: string; side: string; counsel: string | null }[];
  factCounts: FactCounts;
  issueCount: number;
  argumentCount: number;
  timelineCount: number;
  latestRunStatus: RunStatus | null;
  latestRunCompletedAt: string | null;
}

export type FactStatus = "ESTABLISHED" | "DISPUTED" | "MISSING";

export interface Fact {
  id: string;
  factText: string;
  factStatus: FactStatus;
  sourceExcerpt: string | null;
  confidence: number | null;
}

export interface TimelineEvent {
  id: string;
  eventDate: string | null;
  eventText: string;
  eventType: string | null;
  sortOrder: number;
}

export interface LegalIssue {
  id: string;
  issueText: string;
  issueType: "PRIMARY" | "SECONDARY";
  rank: number;
  importanceScore: number | null;
}

export interface Argument {
  id: string;
  partySide: "PETITIONER" | "RESPONDENT";
  argumentText: string;
  strength: "STRONG" | "MODERATE" | "WEAK" | null;
  sourceExcerpt: string | null;
}

export interface ArgumentsView {
  petitioner: Argument[];
  respondent: Argument[];
}

export interface Irac {
  id: string;
  issue: string;
  rule: string;
  application: string;
  conclusion: string;
}

export interface CaseStrength {
  overallScore: number | null;
  strong: string[];
  weak: string[];
  missingEvidence: string[];
  openQuestions: string[];
}

export interface Risk {
  id: string;
  riskType: string;
  severity: "HIGH" | "MEDIUM" | "LOW" | null;
  description: string;
}

export interface Readiness {
  evidenceReadiness: number | null;
  witnessReadiness: number | null;
  researchReadiness: number | null;
  hearingReadiness: number | null;
  overallReadiness: number | null;
}

export interface CaseAnalytics {
  strength: CaseStrength | null;
  risks: Risk[];
  readiness: Readiness | null;
}

export interface MatterReadiness {
  caseId: string;
  title: string;
  overallReadiness: number | null;
  highRisks: number;
}

export interface Portfolio {
  caseCount: number;
  avgReadiness: number | null;
  highRiskCases: number;
  matters: MatterReadiness[];
  riskByType: Record<string, number>;
}
