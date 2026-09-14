export type UUID = string;

export type AccountType = "CHECKING" | "SAVINGS";
export type AccountStatus = "ACTIVE" | "FROZEN" | "CLOSED";
export type CustomerStatus = "ACTIVE" | "SUSPENDED" | "CLOSED";

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
}

export interface RegisterResponse {
  userId: string;
  email: string;
  role: string;
  message: string;
}

export interface CustomerProfilePayload {
  firstName: string | null;
  lastName: string | null;
  phoneNumber: string | null;
  dateOfBirth: string | null;
}

export interface CustomerProfileResponse extends CustomerProfilePayload {
  id: UUID;
  authUserId: UUID;
  status: CustomerStatus;
  createdAt: string;
  updatedAt: string;
}

export interface AccountResponse {
  id: UUID;
  accountNumber: string;
  accountType: AccountType;
  currency: string;
  status: AccountStatus;
  createdAt: string;
  updatedAt: string;
}

export interface LedgerBalanceResult {
  accountId: UUID;
  currency: string;
  postedBalance: number;
}

export interface LedgerTransactionEntry {
  postingId: UUID;
  journalId: UUID;
  referenceType: string;
  referenceId: UUID;
  entryType: string;
  amount: number;
  currency: string;
  createdAt: string;
}

export interface LedgerTransactionPage {
  accountId: UUID;
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  transactions: LedgerTransactionEntry[];
}

export interface TransferResult {
  requestId: UUID;
  journalId: UUID;
  sourceAccountId: UUID;
  destinationAccountId: UUID;
  amount: number;
  currency: string;
  createdAt: string;
}

export interface ApiErrorBody {
  error?: string;
  message?: string;
  fieldErrors?: Record<string, string>;
}
