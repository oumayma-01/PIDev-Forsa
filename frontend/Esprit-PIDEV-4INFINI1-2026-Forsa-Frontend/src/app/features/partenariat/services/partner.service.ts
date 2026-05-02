import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type {
  ClientCashback, Partner, PartnerAnalytics,
  PartnerReview, PartnerTransaction, PartnerType,
} from '../../../core/models/forsa.models';

@Injectable({ providedIn: 'root' })
export class PartnerService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl      = `${environment.apiBaseUrl}/partners`;
  private readonly reviewsUrl  = `${environment.apiBaseUrl}/partner-reviews`;
  private readonly txUrl       = `${environment.apiBaseUrl}/partner-transactions`;
  private readonly cashbackUrl = `${environment.apiBaseUrl}/cashback`;

  // ── CRUD ──────────────────────────────────────────────────────────────────
  getAllPartners(): Observable<Partner[]> {
    return this.http.get<Partner[]>(this.apiUrl);
  }

  getPartnerById(id: number): Observable<Partner> {
    return this.http.get<Partner>(`${this.apiUrl}/${id}`);
  }

  createPartner(partner: Partial<Partner>): Observable<Partner> {
    return this.http.post<Partner>(this.apiUrl, partner);
  }

  updatePartner(id: number, partner: Partial<Partner>): Observable<Partner> {
    return this.http.put<Partner>(`${this.apiUrl}/${id}`, partner);
  }

  deletePartner(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ── Filters ───────────────────────────────────────────────────────────────
  getActivePartners(): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}/status/ACTIVE`);
  }

  getPartnersByType(type: PartnerType): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}/type/${type}`);
  }

  getTopRated(): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}/top-rated`);
  }

  // ── Status management ─────────────────────────────────────────────────────
  activatePartner(id: number): Observable<Partner> {
    return this.http.post<Partner>(`${this.apiUrl}/${id}/activate`, {});
  }

  suspendPartner(id: number, reason: string): Observable<Partner> {
    return this.http.post<Partner>(`${this.apiUrl}/${id}/suspend`, null, {
      params: { reason },
    });
  }

  reactivatePartner(id: number): Observable<Partner> {
    return this.http.post<Partner>(`${this.apiUrl}/${id}/reactivate`, {});
  }

  // ── Reviews ───────────────────────────────────────────────────────────────
  getReviews(partnerId: number): Observable<PartnerReview[]> {
    return this.http.get<PartnerReview[]>(`${this.reviewsUrl}/partner/${partnerId}`);
  }

  addReview(review: Partial<PartnerReview>): Observable<PartnerReview> {
    return this.http.post<PartnerReview>(this.reviewsUrl, review);
  }

  deleteReview(reviewId: number): Observable<void> {
    return this.http.delete<void>(`${this.reviewsUrl}/${reviewId}`);
  }

  getAverageRating(partnerId: number): Observable<number> {
    return this.http.get<number>(`${this.reviewsUrl}/partner/${partnerId}/rating`);
  }

  // ── Transactions ──────────────────────────────────────────────────────────
  getClientTransactions(clientId: number): Observable<PartnerTransaction[]> {
    return this.http.get<PartnerTransaction[]>(`${this.txUrl}/client/${clientId}`);
  }

  getPartnerTransactions(partnerId: number): Observable<PartnerTransaction[]> {
    return this.http.get<PartnerTransaction[]>(`${this.txUrl}/partner/${partnerId}`);
  }

  createTransaction(tx: {
    clientId: number;
    qrSessionId: string;
    amount: number;
    durationMonths?: number;
    description?: string;
  }): Observable<PartnerTransaction> {
    return this.http.post<PartnerTransaction>(`${this.txUrl}/create`, tx);
  }

  confirmTransaction(id: number): Observable<PartnerTransaction> {
    return this.http.post<PartnerTransaction>(`${this.txUrl}/${id}/confirm`, {});
  }

  cancelTransaction(id: number, reason: string): Observable<PartnerTransaction> {
    return this.http.post<PartnerTransaction>(`${this.txUrl}/${id}/cancel`, null, {
      params: { reason },
    });
  }

  // ── Cashback ──────────────────────────────────────────────────────────────
  getClientCashback(clientId: number): Observable<ClientCashback> {
    return this.http.get<ClientCashback>(`${this.cashbackUrl}/client/${clientId}`);
  }

  getClientCashbackBalance(clientId: number): Observable<number> {
    return this.http.get<number>(`${this.cashbackUrl}/client/${clientId}/balance`);
  }

  redeemCashback(clientId: number, amount: number): Observable<ClientCashback> {
    return this.http.post<ClientCashback>(
      `${this.cashbackUrl}/client/${clientId}/redeem`,
      null,
      { params: { amount: amount.toString() } }
    );
  }

  useCashback(cashbackId: number, transactionId: number): Observable<void> {
    return this.http.post<void>(`${this.cashbackUrl}/${cashbackId}/use`, null, {
      params: { transactionId: transactionId.toString() },
    });
  }

  // ── Analytics ─────────────────────────────────────────────────────────────
  getPartnerAnalytics(partnerId: number): Observable<PartnerAnalytics> {
    return this.http.get<PartnerAnalytics>(
      `${environment.apiBaseUrl}/partner-analytics/${partnerId}/dashboard`
    );
  }
}