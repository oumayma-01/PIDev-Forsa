import {
  ClaimsByStatusDTO,
  ClaimsByTypeDTO,
  ClaimsDashboardDTO,
  MonthlyClaimTrendDTO,
  TopClaimDTO,
} from '../models/claims-dashboard.models';
import { InsuranceOverviewDTO, ProductPopularityDTO } from '../models/insurance-analytics.models';

type JsonObject = Record<string, unknown>;

function asObj(v: unknown): JsonObject {
  return typeof v === 'object' && v !== null ? (v as JsonObject) : {};
}

function num(v: unknown, fallback = 0): number {
  const n = Number(v);
  return Number.isFinite(n) ? n : fallback;
}

function str(v: unknown): string {
  return v == null ? '' : String(v);
}

function pick(obj: unknown, ...keys: string[]): unknown {
  const o = asObj(obj);
  for (const k of keys) {
    const v = o[k];
    if (v !== undefined) return v;
  }
  return undefined;
}

/** Accepts ISO strings, epoch ms, Jackson date arrays, or Java LocalDate-like objects. */
export function coerceClaimDateValue(v: unknown): string {
  if (v == null) return '';
  if (typeof v === 'string') return v;
  if (typeof v === 'number') return new Date(v).toISOString();
  if (v instanceof Date) return v.toISOString();
  if (Array.isArray(v) && v.length >= 3) {
    const y = num(v[0]);
    const m = num(v[1]);
    const d = num(v[2]);
    if ([y, m, d].every((x) => Number.isFinite(x) && x > 0)) {
      return new Date(y, m - 1, d).toISOString();
    }
  }
  const o = asObj(v);
  const y = num(pick(o, 'year'));
  const m = num(pick(o, 'monthValue', 'month'));
  const d = num(pick(o, 'dayOfMonth', 'day'));
  if (y && m && d) {
    return new Date(y, m - 1, d).toISOString();
  }
  return '';
}

function normalizeTopClaim(c: unknown, index: number): TopClaimDTO {
  const row = asObj(c);
  return {
    id: num(pick(row, 'id', 'claimId'), index),
    claimNumber: str(pick(row, 'claimNumber', 'claim_number')),
    policyNumber: str(pick(row, 'policyNumber', 'policy_number')),
    claimAmount: num(pick(row, 'claimAmount', 'claim_amount'), 0),
    status: str(pick(row, 'status')),
    claimDate: coerceClaimDateValue(pick(row, 'claimDate', 'claim_date')),
  };
}

export function normalizeClaimsDashboardDTO(ca: unknown): ClaimsDashboardDTO {
  const raw = asObj(ca);
  const topRaw = pick(raw, 'topClaims', 'top_claims');
  const topClaims: TopClaimDTO[] = (Array.isArray(topRaw) ? topRaw : []).map((c, i) => normalizeTopClaim(c, i));

  const byStatusRaw = pick(raw, 'claimsByStatus', 'claims_by_status');
  const claimsByStatus: ClaimsByStatusDTO[] = (Array.isArray(byStatusRaw) ? byStatusRaw : []).map((s) => {
    const o = asObj(s);
    return { status: str(pick(o, 'status')), count: num(pick(o, 'count'), 0) };
  });

  const byTypeRaw = pick(raw, 'claimsByType', 'claims_by_type');
  const claimsByType: ClaimsByTypeDTO[] = (Array.isArray(byTypeRaw) ? byTypeRaw : []).map((t) => {
    const o = asObj(t);
    return {
      policyType: str(pick(o, 'policyType', 'policy_type')),
      count: num(pick(o, 'count'), 0),
      totalAmount: num(pick(o, 'totalAmount', 'total_amount'), 0),
      averageAmount: num(pick(o, 'averageAmount', 'average_amount'), 0),
    };
  });

  const trendsRaw = pick(raw, 'monthlyTrends', 'monthly_trends');
  const monthlyTrends: MonthlyClaimTrendDTO[] = (Array.isArray(trendsRaw) ? trendsRaw : []).map((t) => {
    const o = asObj(t);
    return {
      year: num(pick(o, 'year'), new Date().getFullYear()),
      month: num(pick(o, 'month'), 1),
      count: num(pick(o, 'count'), 0),
      totalAmount: num(pick(o, 'totalAmount', 'total_amount'), 0),
    };
  });

  return {
    totalClaims: num(pick(raw, 'totalClaims', 'total_claims'), 0),
    totalClaimAmount: num(pick(raw, 'totalClaimAmount', 'total_claim_amount'), 0),
    averageClaimAmount: num(pick(raw, 'averageClaimAmount', 'average_claim_amount'), 0),
    approvalRate: num(pick(raw, 'approvalRate', 'approval_rate'), 0),
    claimsByStatus,
    claimsByType,
    monthlyTrends,
    topClaims,
  };
}

export function normalizeInsuranceOverviewDTO(raw: unknown): InsuranceOverviewDTO {
  const r = asObj(raw);
  const claimsBlock = pick(r, 'claimsAnalytics', 'claims_analytics');

  const popularRaw = pick(r, 'popularProducts', 'popular_products');
  const popularProducts: ProductPopularityDTO[] = (Array.isArray(popularRaw) ? popularRaw : []).map((p) => {
    const o = asObj(p);
    return {
      productName: str(pick(o, 'productName', 'product_name')),
      policyCount: num(pick(o, 'policyCount', 'policy_count'), 0),
      totalRevenue: num(pick(o, 'totalRevenue', 'total_revenue'), 0),
    };
  });

  return {
    totalProducts: num(pick(r, 'totalProducts', 'total_products'), 0),
    activePolicies: num(pick(r, 'activePolicies', 'active_policies'), 0),
    canceledPolicies: num(pick(r, 'canceledPolicies', 'canceled_policies'), 0),
    pendingPolicies: num(pick(r, 'pendingPolicies', 'pending_policies'), 0),
    suspendedPolicies: num(pick(r, 'suspendedPolicies', 'suspended_policies'), 0),
    totalPremiumRevenue: num(pick(r, 'totalPremiumRevenue', 'total_premium_revenue'), 0),
    popularProducts,
    claimsAnalytics: normalizeClaimsDashboardDTO(claimsBlock),
  };
}
