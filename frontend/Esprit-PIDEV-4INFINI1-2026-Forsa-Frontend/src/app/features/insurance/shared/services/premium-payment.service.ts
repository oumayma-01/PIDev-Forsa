import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { PremiumPayment, InsurancePolicy } from '../models/insurance.models';

@Injectable({ providedIn: 'root' })
export class PremiumPaymentService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/premium-payment`;

  getAll(): Observable<PremiumPayment[]> {
    return this.http.get<PremiumPayment[]>(`${this.base}/retrieve-all-premium-payments`);
  }

  getMyPayments(): Observable<PremiumPayment[]> {
    return this.http.get<PremiumPayment[]>(`${this.base}/my-payments`);
  }

  getById(id: number): Observable<PremiumPayment> {
    return this.http.get<PremiumPayment>(`${this.base}/retrieve-premium-payment/${id}`);
  }

  create(payment: PremiumPayment): Observable<PremiumPayment> {
    return this.http.post<PremiumPayment>(`${this.base}/add-premium-payment`, payment);
  }

  update(payment: PremiumPayment): Observable<PremiumPayment> {
    return this.http.put<PremiumPayment>(`${this.base}/modify-premium-payment`, payment);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/remove-premium-payment/${id}`);
  }

  affectPaymentsToPolicy(policyId: number, paymentIds: number[]): Observable<InsurancePolicy> {
    return this.http.put<InsurancePolicy>(`${this.base}/affect-premium-payments/${policyId}`, paymentIds);
  }

  createStripeSession(paymentData: { 
    amount: number, 
    currency: string, 
    productName: string, 
    successUrl: string, 
    cancelUrl: string 
  }): Observable<{ sessionUrl: string }> {
    return this.http.post<{ sessionUrl: string }>(`${environment.apiBaseUrl}/payments/create-checkout-session`, paymentData);
  }
}
