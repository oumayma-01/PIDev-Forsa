// ── Enums ────────────────────────────────────────────────────────────────────

export type TransactionType = 'DEPOSIT' | 'WITHDRAW' | 'TRANSFER_IN' | 'TRANSFER_OUT' | 'INTEREST';

export type AccountStatus = 'ACTIVE' | 'BLOCKED';

export type AccountType = 'SAVINGS' | 'INVESTMENT';

// ── Entités principales ───────────────────────────────────────────────────────

export interface Wallet {
  id: number | string;
  ownerId: number | string;
  balance: number;
  transactions: Transaction[];
}

export interface Transaction {
  id: number | string;
  amount: number;
  date: string; // LocalDateTime → string en JSON
  type: TransactionType;
  description?: string;
  status?: string;
}

export interface Account {
  id: number;
  type: AccountType;
  status: AccountStatus;
  accountHolderName: string;
  wallet: Wallet;
}

export interface Activity {
  id: number;
  action: string;
  timestamp: string;
}

// ── DTOs (réponses spéciales du backend) ─────────────────────────────────────

export interface WalletStatisticsDTO {
  balance: number;
  totalDeposits: number;
  totalWithdrawals: number;
  numberOfTransactions: number;
}

export interface WalletForecastDTO {
  currentBalance: number;
  predictedBalance: number;
  forecastDays: number;
  trend: string;
  explanation: string;
}

export interface AccountTypeAdviceDTO {
  accountId: number;
  currentType: string;
  recommendedType: string;
  changeAdvised: boolean;
  reasoning: string;
}
