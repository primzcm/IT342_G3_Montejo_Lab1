import axios from "axios";

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

export async function apiRequest<T>(
  path: string,
  { method = "GET", body, token }: { method?: string; body?: unknown; token?: string } = {}
): Promise<T> {
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
