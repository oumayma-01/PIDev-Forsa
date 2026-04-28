import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  AmortizationScheduleResponse,
  AmortizationType,
  CreditRequestApi,
  RejectCreditPayload,
  RepaymentScheduleApi,
} from '../models/credit-api.model';

@Injectable({ providedIn: 'root' })
export class CreditApiService {
  private readonly http = inject(HttpClient);

  /** ADMIN only: `GET /api/credits` */
  listAllCredits(): Observable<CreditRequestApi[]> {
    return this.http.get<CreditRequestApi[]>(`${environment.apiBaseUrl}/credits`);
  }

  /** AGENT only: `GET /api/credits/pending` */
  listPendingCredits(): Observable<CreditRequestApi[]> {
    return this.http.get<CreditRequestApi[]>(`${environment.apiBaseUrl}/credits/pending`);
  }

  /** Any authenticated role: `GET /api/credits/me` */
  listMyCredits(): Observable<CreditRequestApi[]> {
    return this.http.get<CreditRequestApi[]>(`${environment.apiBaseUrl}/credits/me`);
  }

  /** Any authenticated role: `GET /api/credits/{id}` */
  getCreditById(id: number): Observable<CreditRequestApi> {
    return this.http.get<CreditRequestApi>(`${environment.apiBaseUrl}/credits/${id}`);
  }

  /** Any authenticated role: `POST /api/credits/with-health-report` (multipart) */
  createCreditWithHealthReport(input: {
    amountRequested: number;
    durationMonths: number;
    typeCalcul: AmortizationType;
    healthReport: File;
    guarantorName: string;
    guarantorCin: string;
    guarantorBankAccount: string;
    guarantorPhoto: File;
  }): Observable<CreditRequestApi> {
    const body = new FormData();
    body.append('amountRequested', String(input.amountRequested));
    body.append('durationMonths', String(input.durationMonths));
    body.append('typeCalcul', input.typeCalcul);
    body.append('healthReport', input.healthReport, input.healthReport.name);
    body.append('guarantorName', input.guarantorName);
    body.append('guarantorCin', input.guarantorCin);
    body.append('guarantorBankAccount', input.guarantorBankAccount);
    body.append('guarantorPhoto', input.guarantorPhoto, input.guarantorPhoto.name);

    return this.http.post<CreditRequestApi>(`${environment.apiBaseUrl}/credits/with-health-report`, body);
  }

  /** AGENT/ADMIN only: `GET /api/credits/{id}/guarantor-photo` — returns decrypted image */
  getGuarantorPhoto(creditId: number): Observable<Blob> {
    return this.http.get(`${environment.apiBaseUrl}/credits/${creditId}/guarantor-photo`, { responseType: 'blob' });
  }

  /** Any authenticated role: `GET /api/credits/simulate` */
  simulateAmortization(input: {
    principal: number;
    rate: number;
    duration: number;
    type: AmortizationType;
  }): Observable<AmortizationScheduleResponse> {
    const params = new HttpParams()
      .set('principal', String(input.principal))
      .set('rate', String(input.rate))
      .set('duration', String(input.duration))
      .set('type', input.type);

    return this.http.get<AmortizationScheduleResponse>(`${environment.apiBaseUrl}/credits/simulate`, { params });
  }

  /** Any authenticated role: `GET /api/credits/{id}/schedule` */
  getAmortizationSchedule(creditId: number): Observable<AmortizationScheduleResponse> {
    return this.http.get<AmortizationScheduleResponse>(`${environment.apiBaseUrl}/credits/${creditId}/schedule`);
  }

  /** Any authenticated role: `POST /api/credits/{id}/validate` */
  validateCredit(creditId: number): Observable<CreditRequestApi> {
    return this.http.post<CreditRequestApi>(`${environment.apiBaseUrl}/credits/${creditId}/validate`, null);
  }

  /** AGENT only: `POST /api/credits/{id}/approve` */
  approveCredit(creditId: number): Observable<CreditRequestApi> {
    return this.http.post<CreditRequestApi>(`${environment.apiBaseUrl}/credits/${creditId}/approve`, null);
  }

  /** AGENT only: `POST /api/credits/{id}/reject` */
  rejectCredit(creditId: number, payload: RejectCreditPayload): Observable<CreditRequestApi> {
    return this.http.post<CreditRequestApi>(`${environment.apiBaseUrl}/credits/${creditId}/reject`, payload);
  }

  /** `GET /api/repayments/credit/{creditId}` */
  getRepaymentsForCredit(creditId: number): Observable<RepaymentScheduleApi[]> {
    return this.http.get<RepaymentScheduleApi[]>(`${environment.apiBaseUrl}/repayments/credit/${creditId}`);
  }

  /** `PATCH /api/repayments/{id}/pay?amount=` */
  payRepayment(repaymentId: number, amount?: number): Observable<RepaymentScheduleApi> {
    let params = new HttpParams();
    if (amount != null) {
      params = params.set('amount', String(amount));
    }
    return this.http.patch<RepaymentScheduleApi>(`${environment.apiBaseUrl}/repayments/${repaymentId}/pay`, null, { params });
  }

  /** Any authenticated role: `GET /api/gifts/me/award-notification` */
  consumeMyGiftAwardNotification(): Observable<{ show: boolean; amount?: number }> {
    return this.http.get<{ show: boolean; amount?: number }>(`${environment.apiBaseUrl}/gifts/me/award-notification`);
  }
}
