export type UUID = string;

export type AccountType = "CHECKING" | "SAVINGS";
export type AccountStatus = "ACTIVE" | "FROZEN" | "CLOSED";
export type CustomerStatus = "ACTIVE" | "SUSPENDED" | "CLOSED";
export type CardType = "DEBIT" | "CREDIT";
export type CardStatus = "ACTIVE" | "BLOCKED" | "CANCELLED";
export type TransactionType = "CREDIT" | "DEBIT" | "TRANSFER";
export type TransactionStatus = "PENDING" | "POSTED" | "FAILED";
export type LoanStatus =
  | "APPLICATION"
  | "APPROVED"
  | "ACTIVE"
  | "CLOSED"
  | "REJECTED";

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

export interface CardResponse {
  id: UUID;
  authUserId: UUID;
  cardholderName: string;
  type: CardType;
  status: CardStatus;
  maskedNumber: string;
  currency: string;
  expirationMonth: number;
  expirationYear: number;
  createdAt: string;
  updatedAt: string;
}

export interface BeneficiaryResponse {
  id: UUID;
  authUserId: UUID;
  nickname: string;
  accountHolderName: string;
  accountNumber: string;
  bankName: string | null;
  currency: string;
  createdAt: string;
  updatedAt: string;
}

export interface LoanResponse {
  id: UUID;
  authUserId: UUID;
  principalAmount: number;
  annualInterestRate: number;
  termMonths: number;
  currency: string;
  status: LoanStatus;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionResponse {
  id: UUID;
  authUserId: UUID;
  accountId: UUID;
  counterpartyAccountId: UUID | null;
  amount: number;
  currency: string;
  description: string | null;
  type: TransactionType;
  status: TransactionStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ApiErrorBody {
  error?: string;
  message?: string;
  fieldErrors?: Record<string, string>;
}
