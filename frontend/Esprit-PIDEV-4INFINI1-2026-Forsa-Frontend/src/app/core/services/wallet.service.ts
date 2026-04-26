import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  WalletStatisticsDTO,
  WalletForecastDTO,
  Transaction,
  Activity,
} from '../models/wallet.models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class WalletService {
  // `environment.apiBaseUrl` already contains the `/api` context path for this project
  private base = `${environment.apiBaseUrl}/accounts`;

  constructor(private http: HttpClient) {}

  getStatistics(accountId: number | string): Observable<WalletStatisticsDTO> {
    return this.http.get<WalletStatisticsDTO>(`${this.base}/${accountId}/statistics`);
  }

  forecastBalance(accountId: number | string, days = 30): Observable<WalletForecastDTO> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<WalletForecastDTO>(`${this.base}/${accountId}/forecast`, { params });
  }

  getTransactions(accountId: number | string, type?: string): Observable<Transaction[]> {
    let params = new HttpParams();
    if (type) params = params.set('type', type);
    return this.http.get<Transaction[]>(`${this.base}/${accountId}/transactions/filter`, { params });
  }

  getActivities(accountId: number | string): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.base}/${accountId}/activities`);
  }

  deposit(accountId: number | string, amount: number): Observable<any> {
    const params = new HttpParams().set('amount', amount.toString());
    return this.http.post(`${this.base}/${accountId}/deposit`, null, { params });
  }

  withdraw(accountId: number | string, amount: number): Observable<any> {
    const params = new HttpParams().set('amount', amount.toString());
    return this.http.post(`${this.base}/${accountId}/withdraw`, null, { params });
  }

  transfer(fromAccountId: number | string, toAccountId: number | string, amount: number): Observable<any> {
    const params = new HttpParams()
      .set('fromAccountId', String(fromAccountId))
      .set('toAccountId', String(toAccountId))
      .set('amount', amount.toString());
    return this.http.post(`${this.base}/transfer`, null, { params });
  }
}
