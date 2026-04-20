export interface User {
  id: string;
  name: string;
  email: string;
  role: 'admin' | 'client' | 'agent';
  avatar: string;
}

export interface CreditRequest {
  id: string;
  clientId: string;
  clientName: string;
  amount: number;
  purpose: string;
  status: 'pending' | 'approved' | 'rejected' | 'disbursed';
  date: string;
  riskScore: number;
}

export interface Transaction {
  id: string;
  type: 'deposit' | 'payment' | 'transfer' | 'withdrawal';
  amount: number;
  date: string;
  description: string;
  status: string;
}

export interface InsurancePolicy {
  id: string;
  type: 'health' | 'auto' | 'life';
  status: 'active' | 'pending';
  premium: number;
  coverage: number;
  startDate: string;
  endDate: string;
}

export interface Complaint {
  id: string;
  subject: string;
  description: string;
  status: 'open' | 'in-progress' | 'resolved';
  priority: 'high' | 'medium' | 'low';
  date: string;
}

/** Mirrors the backend `Partner` entity for `GET/POST /api/partners`. */
export type PartnerType =
  | 'PHARMACIE'
  | 'PARAPHARMACIE'
  | 'LIBRAIRE'
  | 'COOPERATIVE_AGRICOLE'
  | 'EQUIPEMENT_PROFESSIONNEL'
  | 'QUINCAILLERIE'
  | 'GARAGISTE'
  | 'SUPERMARCHE'
  | 'TELECOM'
  | 'AUTRE';

export type PartnerStatus = 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'REJECTED' | 'CLOSED';

export type PartnerBadge = 'BRONZE' | 'SILVER' | 'GOLD' | 'DIAMOND';

export interface Partner {
  id: number;
  businessName: string;
  partnerType: PartnerType;
  registrationNumber: string;
  address: string;
  city: string;
  businessPhone?: string;
  businessEmail?: string;
  description?: string;
  iban: string;
  bankName: string;
  accountHolderName: string;
  status: PartnerStatus;
  qrCodeId?: string;
  maxTransactionAmount: number;
  dailyTransactionLimit: number;
  monthlyTransactionLimit: number;
  commissionRate: number;
  totalAmountProcessed?: number;
  totalTransactionsCount?: number;
  activatedAt?: string;
  suspendedAt?: string;
  suspensionReason?: string;
  contactPersonName?: string;
  contactPersonPhone?: string;
  contactPersonEmail?: string;
  latitude?: number;
  longitude?: number;
  badge?: PartnerBadge;
  averageRating?: number;
  totalReviews?: number;
}

/** Mirrors `RiskCategory` in scoring (ScoreResult.riskCategory). */
export type RiskCategory = 'EXCELLENT' | 'GOOD' | 'MODERATE' | 'RISKY' | 'VERY_RISKY';

/** Mirrors backend `ScoreResult` — POST/GET `/api/scoring/calculate|latest/{clientId}`. */
export interface ScoreResult {
  id: number;
  clientId: number;
  finalScore: number;
  riskCategory: RiskCategory;
  factor1Score: number;
  factor2Score: number;
  factor3Score: number;
  factor4Score: number;
  factor5Score: number;
  factor1Contribution: number;
  factor2Contribution: number;
  factor3Contribution: number;
  factor4Contribution: number;
  factor5Contribution: number;
  calculationDate: string;
  calculationVersion: string;
  calculatedBy: string;
  aiExplanation?: string;
  aiExplanationGeneratedAt?: string;
}

/** Mirrors backend `RiskMetrics` — POST `/api/scoring/risk/{clientId}` (loanAmount, durationMonths). */
export interface RiskMetrics {
  id: number;
  clientId: number;
  loanId?: number;
  scoreResultId: number;
  probabilityOfDefault: number;
  lossGivenDefault: number;
  exposureAtDefault: number;
  expectedLoss: number;
  loanAmount: number;
  loanDurationMonths: number;
  personalizedInterestRate: number;
  collateralValue: number;
  seizeableIncome: number;
  guarantorCapacity: number;
  recoveryCosts: number;
  calculationDate: string;
  calculationVersion: string;
}

/** One demo row: latest score + sample loan risk (mock only). */
export interface ScoringClientDemo {
  clientId: number;
  clientName: string;
  latestScore: ScoreResult;
  sampleLoanRisk: RiskMetrics;
}

// ── AI Scoring (agent Python sur port 5000, proxié via /api/ai-score/*) ───────

/** Corps de la requête envoyée à POST /api/ai-score/calculate/{clientId}. */
export interface AIScoreRequest {
  clientId: number;
  monthlySalary: number;
  stegPaidOnTime: boolean;
  sondePaidOnTime: boolean;
  cinVerified: boolean;
}

/** Niveaux de score retournés par l'agent IA. */
export type AIScoreLevel =
  | 'VERY_LOW'
  | 'LOW'
  | 'MEDIUM'
  | 'GOOD'
  | 'VERY_GOOD'
  | 'EXCELLENT'
  | 'PREMIUM';

/** Features individuelles extraites par l'agent IA. */
export interface AIScoreFeatures {
  f1_salary: number;
  f2_income_stability: number;
  f3_savings_rate: number;
  f4_account_age: number;
  f5_current_balance: number;
  f6_activity: number;
  f7_avg_income: number;
  f8_steg_on_time: number;
  f9_sonede_on_time: number;
  f10_cin_verified: number;
}

/** Détail d'un critère dans score_details (retourné par Python v3). */
export interface AIScoreDetailItem {
  points: number;
  max: number;
  comment: string;
  valeur?: number;
}

/** Réponse complète de l'agent IA (score + explication Mistral). */
export interface AIScoreResponse {
  clientId: number;
  /** Score entre 0 et 1000. */
  score: number;
  scoreLevel: AIScoreLevel;
  /** Montant maximum de crédit autorisé en TND. */
  creditThreshold: number;
  salaryVerified: number;
  /** Explication en langage naturel générée par Mistral. */
  explanation: string;
  features: AIScoreFeatures;
  /** Détail des critères (v3 scoring intelligent). */
  scoreDetails?: Record<string, AIScoreDetailItem>;
  /** Indique si des données bancaires existent en DB. */
  hasDbData?: boolean;
}
