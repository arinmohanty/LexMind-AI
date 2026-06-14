import axios, {
  type AxiosError,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios";
import type { ApiError, ApiResponse, AuthResponse } from "@/types/api";
import {
  clearAuth,
  getAccessToken,
  getRefreshToken,
  setStoredUser,
  setTokens,
} from "@/lib/auth";

const baseURL = import.meta.env.VITE_API_BASE_URL || "";

export const http = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

// Attach the bearer token to every request.
http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// De-duplicated silent refresh on 401.
let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;
  if (!refreshPromise) {
    refreshPromise = axios
      .post<ApiResponse<AuthResponse>>(`${baseURL}/api/v1/auth/refresh`, { refreshToken })
      .then((res) => {
        const auth = res.data.data;
        setTokens(auth.accessToken, auth.refreshToken);
        setStoredUser(auth.user);
        return auth.accessToken;
      })
      .catch(() => null)
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

http.interceptors.response.use(
  (response) => response,
  async (error: AxiosError<ApiResponse<unknown>>) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    if (error.response?.status === 401 && original && !original._retry && getRefreshToken()) {
      original._retry = true;
      const newToken = await refreshAccessToken();
      if (newToken) {
        original.headers.set("Authorization", `Bearer ${newToken}`);
        return http(original);
      }
      clearAuth();
      if (window.location.pathname.startsWith("/app")) {
        window.location.href = "/login";
      }
    }
    return Promise.reject(normalizeError(error));
  },
);

export function normalizeError(error: AxiosError<ApiResponse<unknown>>): ApiError {
  const apiError = error.response?.data?.error;
  if (apiError) return apiError;
  return { code: "NETWORK_ERROR", message: error.message || "Network error" };
}

// ---- typed helpers (unwrap the ApiResponse envelope) ----

export async function apiGet<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.get<ApiResponse<T>>(url, config);
  return res.data.data;
}

export async function apiPost<T>(url: string, body?: unknown, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.post<ApiResponse<T>>(url, body, config);
  return res.data.data;
}

export async function apiDelete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  const res = await http.delete<ApiResponse<T>>(url, config);
  return res.data.data;
}

export async function apiPostForm<T>(url: string, form: FormData): Promise<T> {
  const res = await http.post<ApiResponse<T>>(url, form, {
    headers: { "Content-Type": "multipart/form-data" },
  });
  return res.data.data;
}
