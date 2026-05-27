import { apiRequest } from "../../shared/api/client";
import type { UpdateUserPayload, User } from "./types";

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
