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
  const payload = contentType.includes("application/json")
    ? await response.json()
    : null;

  if (!response.ok) {
    const validationError =
      payload?.errors && Object.keys(payload.errors).length > 0
        ? Object.values(payload.errors)[0]
        : null;
    const errorMessage =
      validationError ||
      payload?.message ||
      payload?.error ||
      "Request failed. Please try again.";
    throw new Error(errorMessage);
  }

  return payload?.data ?? null;
}

export function registerUser(data) {
  return apiRequest("/api/auth/register", {
    method: "POST",
    body: data
  });
}

export function loginUser(data) {
  return apiRequest("/api/auth/login", {
    method: "POST",
    body: data
  });
}

export function fetchCurrentUser(token) {
  return apiRequest("/api/user/me", {
    token
  });
}

export function logoutUser(token) {
  return apiRequest("/api/auth/logout", {
    method: "POST",
    token
  });
}
