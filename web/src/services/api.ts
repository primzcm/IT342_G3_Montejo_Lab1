import axios, { AxiosError } from "axios";
import type {
  AuthResponse,
  CreateProjectPayload,
  JoinRequest,
  ProjectDetail,
  ProjectMessage,
  ProjectSummary,
  UpdateProjectPayload,
  UpdateUserPayload,
  User
} from "../types";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

type ApiEnvelope<T> = {
  success: boolean;
  data: T;
  error?: {
    message?: string;
    details?: string | Record<string, string>;
  };
};

const api = axios.create({
  baseURL: API_BASE_URL
});

function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const responsePayload = error.response?.data as ApiEnvelope<unknown> | undefined;
    const details = responsePayload?.error?.details;
    const detailMessage =
      typeof details === "string"
        ? details
        : details && typeof details === "object" && Object.keys(details).length > 0
          ? Object.values(details)[0]
          : null;
    return detailMessage || responsePayload?.error?.message || error.message || "Request failed. Please try again.";
  }

  if (error instanceof Error) {
    return error.message;
  }

  return "Request failed. Please try again.";
}

function enrichError(error: unknown): never {
  const message = extractErrorMessage(error);
  const wrapped = new Error(message) as Error & { status?: number; payload?: unknown };

  if (axios.isAxiosError(error)) {
    wrapped.status = error.response?.status;
    wrapped.payload = error.response?.data;
  }

  throw wrapped;
}

async function apiRequest<T>(path: string, { method = "GET", body, token }: { method?: string; body?: unknown; token?: string } = {}): Promise<T> {
  try {
    const response = await api.request<ApiEnvelope<T>>({
      url: path,
      method,
      data: body,
      headers: token ? { Authorization: `Bearer ${token}` } : undefined
    });

    return response.data.data;
  } catch (error) {
    enrichError(error);
  }
}

export function registerUser(data: { firstname: string; lastname: string; email: string; password: string }) {
  return apiRequest<AuthResponse>("/api/v1/auth/register", {
    method: "POST",
    body: data
  });
}

export function loginUser(data: { email: string; password: string }) {
  return apiRequest<AuthResponse>("/api/v1/auth/login", {
    method: "POST",
    body: data
  });
}

export function fetchCurrentUser(token: string) {
  return apiRequest<User>("/api/v1/user/me", { token });
}

export function updateCurrentUser(token: string, data: UpdateUserPayload) {
  return apiRequest<User>("/api/v1/user/me", {
    method: "PUT",
    token,
    body: data
  });
}

export function fetchProjects(token: string) {
  return apiRequest<ProjectSummary[]>("/api/v1/projects", { token });
}

export function fetchProjectDetail(token: string, projectId: string | number) {
  return apiRequest<ProjectDetail>(`/api/v1/projects/${projectId}`, { token });
}

export function createProject(token: string, data: CreateProjectPayload) {
  return apiRequest<ProjectSummary>("/api/v1/projects", {
    method: "POST",
    token,
    body: data
  });
}

export function updateProject(token: string, projectId: string | number, data: UpdateProjectPayload) {
  return apiRequest<ProjectSummary>(`/api/v1/projects/${projectId}`, {
    method: "PUT",
    token,
    body: data
  });
}

export function deleteProject(token: string, projectId: string | number) {
  return apiRequest<null>(`/api/v1/projects/${projectId}`, {
    method: "DELETE",
    token
  });
}

export function requestJoinProject(token: string, projectId: string | number, data: { message: string }) {
  return apiRequest<JoinRequest>(`/api/v1/projects/${projectId}/requests`, {
    method: "POST",
    token,
    body: data
  });
}

export function fetchProjectRequests(token: string, projectId: string | number) {
  return apiRequest<JoinRequest[]>(`/api/v1/projects/${projectId}/requests`, { token });
}

export function fetchProjectMessages(token: string, projectId: string | number) {
  return apiRequest<ProjectMessage[]>(`/api/v1/projects/${projectId}/messages`, { token });
}

export function createProjectMessage(token: string, projectId: string | number, data: { content: string }) {
  return apiRequest<ProjectMessage>(`/api/v1/projects/${projectId}/messages`, {
    method: "POST",
    token,
    body: data
  });
}

export function approveJoinRequest(token: string, requestId: string | number) {
  return apiRequest<JoinRequest>(`/api/v1/requests/${requestId}/approve`, {
    method: "PUT",
    token
  });
}

export function rejectJoinRequest(token: string, requestId: string | number) {
  return apiRequest<JoinRequest>(`/api/v1/requests/${requestId}/reject`, {
    method: "PUT",
    token
  });
}

export function logoutUser({ accessToken, refreshToken }: { accessToken: string; refreshToken: string }) {
  return apiRequest<null>("/api/v1/auth/logout", {
    method: "POST",
    token: accessToken,
    body: { refreshToken }
  });
}
