import { apiGet, apiPost } from "@/lib/apiClient";
import type { AuthResponse, User } from "@/types/api";

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  fullName: string;
  role: string;
  organizationName?: string;
}

export const authApi = {
  login: (body: LoginPayload) => apiPost<AuthResponse>("/api/v1/auth/login", body),
  register: (body: RegisterPayload) => apiPost<AuthResponse>("/api/v1/auth/register", body),
  me: () => apiGet<User>("/api/v1/auth/me"),
};
