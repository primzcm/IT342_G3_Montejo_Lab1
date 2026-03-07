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
    throw new Error(errorMessage);
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

export function logoutUser({ accessToken, refreshToken }) {
  return apiRequest("/api/v1/auth/logout", {
    method: "POST",
    token: accessToken,
    body: { refreshToken }
  });
}
