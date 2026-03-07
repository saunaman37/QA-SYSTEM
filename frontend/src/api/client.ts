import type { ErrorResponse, FieldError } from '../types';

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export class ApiError extends Error {
  status: number;
  error: string;
  timestamp: string;
  fieldErrors?: FieldError[];

  constructor(res: ErrorResponse) {
    super(res.message);
    this.status = res.status;
    this.error = res.error;
    this.timestamp = res.timestamp;
    this.fieldErrors = res.fieldErrors;
  }
}

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });
  if (!res.ok) {
    const errBody: ErrorResponse = await res.json();
    throw new ApiError(errBody);
  }
  return res.json() as Promise<T>;
}
