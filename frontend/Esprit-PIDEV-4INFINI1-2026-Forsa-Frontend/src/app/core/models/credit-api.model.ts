export type AmortizationType = 'ANNUITE_CONSTANTE' | 'AMORTISSEMENT_CONSTANT';

export type CreditStatus = 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'ACTIVE' | 'REPAID' | 'DEFAULTED';

export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH';

export interface BackendUserRef {
  id: number;
  username?: string;
  email?: string;
}

export interface CreditRequestApi {
  id: number;
  amountRequested: number;
  interestRate?: number | null;
  durationMonths?: number | null;
  status?: CreditStatus | null;
  requestDate?: string | null;
  agentId?: number | null;
  user?: BackendUserRef | null;

  typeCalcul?: AmortizationType | null;

  remainingBalance?: number | null;
  paidInstallments?: number | null;

  isRisky?: boolean | null;
  riskLevel?: RiskLevel | null;
  scoredAt?: string | null;

  healthReportPath?: string | null;
  originalHealthReportFilename?: string | null;

  insuranceRate?: number | null;
  insuranceAmount?: number | null;
  insuranceIsReject?: boolean | null;
  insuranceRating?: string | null;
  insuranceScoringReport?: string | null;
  insurancePaidAt?: string | null;

  globalDecision?: string | null;
  globalPdfPath?: string | null;
  fraudReportPath?: string | null;
}

export interface AmortizationScheduleResponse {
  creditId?: number;
  calculationType: AmortizationType;
  principal: number;
  annualRatePercent: number;
  durationMonths: number;
  totalInterest: number;
  totalAmount: number;
  periods: AmortizationPeriodDetail[];
}

export interface AmortizationPeriodDetail {
  monthNumber: number;
  principalPayment: number;
  interestPayment: number;
  totalPayment: number;
  remainingBalance: number;
}

export type RepaymentStatus = 'PENDING' | 'PAID' | 'LATE';
export type LineType = 'NORMAL' | 'PENALTY';

export interface RepaymentScheduleApi {
  id: number;
  dueDate: string;
  paidDate?: string | null;

  totalAmount: number;
  principalPart: number;
  interestPart: number;
  remainingBalance: number;

  status: RepaymentStatus;
  lineType?: LineType;

  creditRequest?: { id: number } | null;
}

export interface RejectCreditPayload {
  reason: string;
}
