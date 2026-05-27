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

export interface ProjectMember {
  userId: number;
  name: string;
  joinedAt: string;
}

export interface JoinRequest {
  id: number;
  projectId: number;
  requesterId: number;
  requesterName: string;
  status: string;
  message?: string | null;
  createdAt: string;
  reviewedAt?: string | null;
}

export interface ProjectMessage {
  id: number;
  projectId: number;
  authorId: number;
  authorName: string;
  content: string;
  createdAt: string;
}

export interface ProjectSummary {
  id: number;
  title: string;
  description: string;
  category: string;
  rolesNeeded: string;
  requiredSkills: string[];
  status: string;
  createdAt: string;
  ownerId: number;
  ownerName: string;
  owner: boolean;
  joined: boolean;
  joinRequested: boolean;
}

export interface ProjectDetail extends ProjectSummary {
  members: ProjectMember[];
}

export interface AuthResponse {
  user: User;
  accessToken: string;
  refreshToken: string;
}

export interface CreateProjectPayload {
  title: string;
  category: string;
  rolesNeeded: string;
  requiredSkills: string[];
  description: string;
}

export interface UpdateProjectPayload extends CreateProjectPayload {
  status: "OPEN" | "CLOSED";
}

export interface UpdateUserPayload {
  firstname: string;
  lastname: string;
  bio: string;
  skills: string;
}
