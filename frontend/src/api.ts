import { logout, token } from "./auth";
import type {
  AccountResponse,
  AccountStatus,
  AccountType,
  ApiErrorBody,
  CustomerProfilePayload,
  CustomerProfileResponse,
  LedgerBalanceResult,
  LedgerTransactionPage,
  LoginResponse,
  RegisterResponse,
  TransferResult,
} from "./types";

export class ApiError extends Error {
  constructor(
    readonly status: number,
    readonly body: ApiErrorBody,
  ) {
    super(body.message ?? `HTTP ${status}`);
  }
}

async function request<T>(
  path: string,
  init: RequestInit = {},
  secure = true,
): Promise<T> {
  const headers = new Headers(init.headers);
  headers.set("Accept", "application/json");

  if (init.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (secure) {
    const accessToken = token();

    if (!accessToken) {
      logout();
      window.location.assign("/login");
      throw new Error("Authentication required");
    }

    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(path, { ...init, headers });

  if (secure && response.status === 401) {
    logout();
    window.location.assign("/login");
    throw new Error("Session expired");
  }

  const contentType = response.headers.get("content-type") ?? "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : null;

  if (!response.ok) {
    throw new ApiError(
      response.status,
      body && typeof body === "object"
        ? body as ApiErrorBody
        : {},
    );
  }

  return body as T;
}

export const api = {
  login(email: string, password: string) {
    return request<LoginResponse>(
      "/api/v1/auth/login",
      {
        method: "POST",
        body: JSON.stringify({ email, password }),
      },
      false,
    );
  },

  register(email: string, password: string) {
    return request<RegisterResponse>(
      "/api/v1/auth/register",
      {
        method: "POST",
        body: JSON.stringify({ email, password }),
      },
      false,
    );
  },

  profile() {
    return request<CustomerProfileResponse>("/api/v1/customers/me");
  },

  createProfile(body: CustomerProfilePayload) {
    return request<CustomerProfileResponse>(
      "/api/v1/customers/me",
      { method: "POST", body: JSON.stringify(body) },
    );
  },

  updateProfile(body: CustomerProfilePayload) {
    return request<CustomerProfileResponse>(
      "/api/v1/customers/me",
      { method: "PUT", body: JSON.stringify(body) },
    );
  },

  accounts() {
    return request<AccountResponse[]>("/api/v1/accounts");
  },

  account(id: string) {
    return request<AccountResponse>(
      `/api/v1/accounts/${encodeURIComponent(id)}`,
    );
  },

  createAccount(accountType: AccountType, currency: string) {
    return request<AccountResponse>(
      "/api/v1/accounts",
      {
        method: "POST",
        body: JSON.stringify({ accountType, currency }),
      },
    );
  },

  updateStatus(id: string, status: AccountStatus) {
    return request<AccountResponse>(
      `/api/v1/accounts/${encodeURIComponent(id)}/status`,
      {
        method: "PATCH",
        body: JSON.stringify({ status }),
      },
    );
  },

  balance(id: string) {
    return request<LedgerBalanceResult>(
      `/api/v1/accounts/${encodeURIComponent(id)}/balance`,
    );
  },

  transactions(id: string, page = 0, size = 20) {
    return request<LedgerTransactionPage>(
      `/api/v1/accounts/${encodeURIComponent(id)}/transactions?page=${page}&size=${size}`,
    );
  },

  transfer(
    sourceAccountId: string,
    destinationAccountId: string,
    amount: number,
  ) {
    return request<TransferResult>(
      "/api/v1/transfers",
      {
        method: "POST",
        body: JSON.stringify({
          requestId: crypto.randomUUID(),
          sourceAccountId,
          destinationAccountId,
          amount,
        }),
      },
    );
  },
};
