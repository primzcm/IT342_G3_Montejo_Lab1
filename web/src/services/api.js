const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";

async function apiRequest(path, { method = "GET", body, token } = {}) {
  const headers = {
    "Content-Type": "application/json"
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  const contentType = response.headers.get("content-type") || "";
  const payload = contentType.includes("application/json") ? await response.json() : null;

  if (!response.ok) {
    const details = payload?.error?.details;
    const detailMessage =
      typeof details === "string"
        ? details
        : details && typeof details === "object" && Object.keys(details).length > 0
          ? Object.values(details)[0]
          : null;
    const errorMessage = detailMessage || payload?.error?.message || "Request failed. Please try again.";
    const error = new Error(errorMessage);
    error.status = response.status;
    error.payload = payload;
    throw error;
  }

  return payload?.data ?? null;
}

export function registerUser(data) {
  return apiRequest("/api/v1/auth/register", {
    method: "POST",
    body: data
  });
}

export function loginUser(data) {
  return apiRequest("/api/v1/auth/login", {
    method: "POST",
    body: data
  });
}

export function fetchCurrentUser(token) {
  return apiRequest("/api/v1/user/me", {
    token
  });
}

export function fetchProjects(token) {
  return apiRequest("/api/v1/projects", {
    token
  });
}

export function fetchProjectDetail(token, projectId) {
  return apiRequest(`/api/v1/projects/${projectId}`, {
    token
  });
}

export function createProject(token, data) {
  return apiRequest("/api/v1/projects", {
    method: "POST",
    token,
    body: data
  });
}

export function updateProject(token, projectId, data) {
  return apiRequest(`/api/v1/projects/${projectId}`, {
    method: "PUT",
    token,
    body: data
  });
}

export function deleteProject(token, projectId) {
  return apiRequest(`/api/v1/projects/${projectId}`, {
    method: "DELETE",
    token
  });
}

export function requestJoinProject(token, projectId, data) {
  return apiRequest(`/api/v1/projects/${projectId}/requests`, {
    method: "POST",
    token,
    body: data
  });
}

export function fetchProjectRequests(token, projectId) {
  return apiRequest(`/api/v1/projects/${projectId}/requests`, {
    token
  });
}

export function approveJoinRequest(token, requestId) {
  return apiRequest(`/api/v1/requests/${requestId}/approve`, {
    method: "PUT",
    token
  });
}

export function rejectJoinRequest(token, requestId) {
  return apiRequest(`/api/v1/requests/${requestId}/reject`, {
    method: "PUT",
    token
  });
}

export function logoutUser({ accessToken, refreshToken }) {
  return apiRequest("/api/v1/auth/logout", {
    method: "POST",
    token: accessToken,
    body: { refreshToken }
  });
}
