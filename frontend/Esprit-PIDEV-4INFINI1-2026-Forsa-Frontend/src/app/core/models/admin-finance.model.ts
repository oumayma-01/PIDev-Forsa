export interface AdminFinancialKpis {
  totalPremiumCollected: number;
  totalClaimsPaid: number;
  lossRatioPct: number;
  claimFrequencyPct: number;
  creditPortfolioOutstanding: number;
  creditApprovalRatePct: number;
  weightedAverageInterestRatePct: number;
  portfolioAtRiskPct: number;
  totalWalletBalances: number;
  netCashFlow: number;
  solvencyMarginPct: number;
}

export interface AdminMonthlyPoint {
  month: string;
  premiumCollected: number;
  claimsPaid: number;
  creditDisbursed: number;
}

export interface AdminLabelValue {
  label: string;
  value: number;
}

export interface AdminFinancialDashboard {
  kpis: AdminFinancialKpis;
  monthlyRevenueVsClaims: AdminMonthlyPoint[];
  insuranceStatusSplit: AdminLabelValue[];
  creditStatusSplit: AdminLabelValue[];
  walletFlowSplit: AdminLabelValue[];
}
