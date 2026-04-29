import { ClaimsDashboardDTO } from './claims-dashboard.models';

export interface ProductPopularityDTO {
  productName: string;
  policyCount: number;
  totalRevenue: number;
}

export interface InsuranceOverviewDTO {
  totalProducts: number;
  activePolicies: number;
  canceledPolicies: number;
  pendingPolicies: number;
  totalPremiumRevenue: number;
  popularProducts: ProductPopularityDTO[];
  claimsAnalytics: ClaimsDashboardDTO;
}
