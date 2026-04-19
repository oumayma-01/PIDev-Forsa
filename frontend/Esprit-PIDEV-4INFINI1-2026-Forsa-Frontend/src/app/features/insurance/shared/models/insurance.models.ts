import { ClaimStatus, PaymentStatus, PolicyStatus } from '../enums/insurance.enums';

export interface InsuranceProduct {
  id?: number;
  productName: string;
  policyType: string;
  description?: string;
  premiumAmount: number;
  coverageLimit: number;
  durationMonths: number;
  isActive?: boolean;
  policies?: InsurancePolicy[];
}

export interface InsurancePolicy {
  id?: number;
  policyNumber?: string;
  premiumAmount?: number;
  coverageLimit?: number;
  startDate?: string;
  endDate?: string;
  nextPremiumDueDate?: string;
  status?: PolicyStatus;
  user?: any;
  // Actuarial fields
  purePremium?: number;
  inventoryPremium?: number;
  commercialPremium?: number;
  finalPremium?: number;
  // Risk assessment
  riskScore?: number;
  riskCategory?: string;
  riskCoefficient?: number;
  // Payment details
  paymentFrequency?: string;
  periodicPaymentAmount?: number;
  numberOfPayments?: number;
  effectiveAnnualRate?: number;
  calculationNotes?: string;
  // Relations
  insuranceProduct?: InsuranceProduct;
  premiumPayments?: PremiumPayment[];
  claims?: InsuranceClaim[];
}

export interface InsuranceRiskAssessmentDTO {
  age?: number;
  monthlyIncome?: number;
  healthStatus?: string; // EXCELLENT, GOOD, FAIR, POOR
  hasChronicIllness?: boolean;
  occupationType?: string;
  locationRiskLevel?: string; // LOW, MEDIUM, HIGH
  isSmoker?: boolean;
  dependents?: number;
  
  // Output fields
  riskScore?: number;
  riskCategory?: string;
  riskCoefficient?: number;
}

export interface InsurancePolicyApplicationDTO {
  userId?: number;
  productId: number;
  desiredCoverage: number;
  durationMonths: number;
  paymentFrequency?: string;
  riskProfile?: InsuranceRiskAssessmentDTO;
  notes?: string;
}

export interface PremiumCalculationRequestDTO {
  insuranceType?: string;
  coverageAmount?: number;
  durationMonths?: number;
  paymentFrequency?: string;
  riskProfile?: InsuranceRiskAssessmentDTO;
}

export interface PremiumCalculationResultDTO {
  purePremium?: number;
  inventoryPremium?: number;
  commercialPremium?: number;
  finalPremium?: number;
  periodicPayment?: number;
  paymentFrequency?: string;
  numberOfPayments?: number;
  coverageAmount?: number;
  riskScore?: number;
  riskCategory?: string;
  effectiveAnnualRate?: number;
  calculationMethod?: string;
  additionalNotes?: string;
}

export interface InsuranceAmortizationLineDTO {
  period?: number;
  dueDate?: string;
  payment?: number;
  principalComponent?: number;
  interestComponent?: number;
  remainingBalance?: number;
}

export interface InsuranceAmortizationScheduleDTO {
  totalPremium?: number;
  effectiveAnnualRate?: number;
  numberOfPayments?: number;
  paymentFrequency?: string;
  schedule?: InsuranceAmortizationLineDTO[];
}

export interface InsuranceCompleteQuoteDTO {
  premiumDetails?: PremiumCalculationResultDTO;
  amortizationSchedule?: InsuranceAmortizationScheduleDTO;
}

export interface InsuranceProductComparisonDTO {
  id?: number;
  productName?: string;
  policyType?: string;
  premiumAmount?: number;
  coverageLimit?: number;
  durationMonths?: number;
  description?: string;
  isActive?: boolean;
  valueScore?: number;
  valueRating?: string;
  costPerMonth?: number;
  coveragePerDollar?: number;
}

export interface InsuranceClaim {
  id?: number;
  claimNumber?: string;
  claimDate?: string;
  incidentDate?: string;
  claimAmount?: number;
  approvedAmount?: number;
  description?: string;
  status?: ClaimStatus;
  indemnificationPaid?: number;
  insurancePolicy?: { id: number; policyNumber?: string };
}

export interface PremiumPayment {
  id?: number;
  amount: number;
  dueDate?: string;
  paidDate?: string;
  status?: PaymentStatus;
  transactionId?: number;
  insurancePolicy?: { id: number; policyNumber?: string };
}
