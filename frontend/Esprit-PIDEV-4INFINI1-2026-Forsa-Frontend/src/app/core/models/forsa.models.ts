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

// ── AI scoring (Python service, proxied via /api/ai-score/*) ─────────────────

/** Request body for POST /api/ai-score/calculate/{clientId}. */
export interface AIScoreRequest {
  clientId: number;
  monthlySalary: number;
  stegPaidOnTime: boolean;
  sondePaidOnTime: boolean;
  cinVerified: boolean;
}

/** Score bands returned by the AI agent. */
export type AIScoreLevel =
  | 'VERY_LOW'
  | 'LOW'
  | 'MEDIUM'
  | 'GOOD'
  | 'VERY_GOOD'
  | 'EXCELLENT'
  | 'PREMIUM';

/** Individual feature vector from the AI agent. */
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

/** One criterion row inside score_details (Python v3). */
export interface AIScoreDetailItem {
  points: number;
  max: number;
  comment: string;
  valeur?: number;
}

/** Full AI agent response (score + Mistral explanation). */
export interface AIScoreResponse {
  clientId: number;
  /** Score from 0 to 1000. */
  score: number;
  scoreLevel: AIScoreLevel;
  /** Maximum authorized loan amount in TND. */
  creditThreshold: number;
  salaryVerified: number;
  /** Natural-language explanation from Mistral. */
  explanation: string;
  features: AIScoreFeatures;
  /** Per-criterion breakdown (v3 scoring). */
  scoreDetails?: Record<string, AIScoreDetailItem>;
  /** True when banking-derived features exist in DB. */
  hasDbData?: boolean;
}

// ===== ENUMS =====
export type Category = 'TECHNICAL' | 'FINANCE' | 'SUPPORT' | 'FRAUD' | 'ACCOUNT' | 'CREDIT' | 'OTHER';
export type Priority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ComplaintStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REJECTED';
export type SatisfactionLevel = 'VERY_SATISFIED' | 'SATISFIED' | 'NEUTRAL' | 'DISSATISFIED' | 'VERY_DISSATISFIED';
export type ResponseStatus = 'PENDING' | 'PROCESSED' | 'SENT' | 'FAILED';

// ===== FEEDBACK =====
export interface Feedback {
  id?: number;
  rating: number;
  comment?: string;
  satisfactionLevel?: SatisfactionLevel;
  createdAt?: string;
  isAnonymous: boolean;
  complaint?: ComplaintBackend;
}

// ===== COMPLAINT =====
export interface ComplaintBackend {
  id?: number;
  subject: string;
  description: string;
  category?: Category;
  status?: ComplaintStatus;
  createdAt?: string;
  priority?: Priority;
}

// ===== RESPONSE =====
export interface ComplaintResponse {
  id?: number;
  message: string;
  responderRole: string;
  responderName: string;
  responseDate?: string;
  responseStatus?: ResponseStatus;
  complaint?: ComplaintBackend;
}

// ===== CHATBOT =====
export interface ChatMessage {
  role: 'user' | 'bot';
  content: string;
  timestamp?: Date;
}
