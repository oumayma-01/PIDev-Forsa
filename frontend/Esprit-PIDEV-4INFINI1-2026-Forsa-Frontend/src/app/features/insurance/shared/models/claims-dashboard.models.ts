export interface ClaimsByStatusDTO {
  status: string;
  count: number;
}

export interface ClaimsByTypeDTO {
  policyType: string;
  count: number;
  totalAmount: number;
  averageAmount: number;
}

export interface MonthlyClaimTrendDTO {
  year: number;
  month: number;
  count: number;
  totalAmount: number;
}

export interface TopClaimDTO {
  claimNumber: string;
  policyNumber: string;
  claimAmount: number;
  status: string;
  claimDate: string;
}

export interface ClaimsDashboardDTO {
  totalClaims: number;
  totalClaimAmount: number;
  averageClaimAmount: number;
  approvalRate: number;
  claimsByStatus: ClaimsByStatusDTO[];
  claimsByType: ClaimsByTypeDTO[];
  monthlyTrends: MonthlyClaimTrendDTO[];
  topClaims: TopClaimDTO[];
}
