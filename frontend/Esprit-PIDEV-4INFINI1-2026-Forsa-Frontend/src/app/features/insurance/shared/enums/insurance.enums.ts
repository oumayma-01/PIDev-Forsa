export enum ClaimStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  PAID = 'PAID',
  CLOSED = 'CLOSED',
}

export enum PolicyStatus {
  PENDING = 'PENDING',
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED',
  EXPIRED = 'EXPIRED',
  CANCELLED = 'CANCELLED',
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  LATE = 'LATE',
  FAILED = 'FAILED',
}

export const POLICY_TYPE_OPTIONS = [
  'HEALTH',
  'LIFE',
  'PROPERTY',
  'ACCIDENT',
  'CROP',
  'LIVESTOCK',
  'BUSINESS',
] as const;

export type PolicyType = (typeof POLICY_TYPE_OPTIONS)[number];

export const PAYMENT_FREQUENCY_OPTIONS = [
  'MONTHLY',
  'QUARTERLY',
  'SEMI_ANNUAL',
  'ANNUAL',
] as const;

export type PaymentFrequency = (typeof PAYMENT_FREQUENCY_OPTIONS)[number];
