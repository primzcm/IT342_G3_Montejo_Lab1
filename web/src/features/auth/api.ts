import { apiRequest } from "../../shared/api/client";
import type { AuthResponse } from "./types";

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

export function logoutUser({ accessToken, refreshToken }: { accessToken: string; refreshToken: string }) {
  return apiRequest<null>("/api/v1/auth/logout", {
    method: "POST",
    token: accessToken,
    body: { refreshToken }
  });
}
