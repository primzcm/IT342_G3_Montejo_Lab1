import type { User } from "../users/types";

export interface AuthResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
}
