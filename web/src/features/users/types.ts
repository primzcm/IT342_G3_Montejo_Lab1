export interface User {
  id: number;
  username: string;
  email: string;
  firstname: string;
  lastname: string;
  role: string;
  createdAt: string;
  bio?: string | null;
  skills?: string | null;
}

export interface UpdateUserPayload {
  firstname: string;
  lastname: string;
  bio: string;
  skills: string;
}
