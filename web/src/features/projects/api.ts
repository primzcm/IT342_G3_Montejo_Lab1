import { apiRequest } from "../../shared/api/client";
import type {
  CreateProjectPayload,
  JoinRequest,
  ProjectDetail,
  ProjectMessage,
  ProjectSummary,
  UpdateProjectPayload
} from "./types";

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
