import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  Account,
  Transaction,
  WalletStatisticsDTO,
  WalletForecastDTO,
  AccountTypeAdviceDTO,
} from '../models/wallet.models';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/accounts`;

  getAccountsByOwner(ownerId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/owner/${ownerId}`);
  }

  getAccount(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.base}/${id}`);
  }

  deposit(accountId: number, amount: number): Observable<string> {
    const params = new HttpParams().set('amount', amount.toString());
    return this.http.post<string>(`${this.base}/${accountId}/deposit`, null, { params });
  }

  withdraw(accountId: number, amount: number): Observable<string> {
    const params = new HttpParams().set('amount', amount.toString());
    return this.http.post<string>(`${this.base}/${accountId}/withdraw`, null, { params });
  }

  transfer(fromAccountId: number, toAccountId: number, amount: number): Observable<string> {
    const params = new HttpParams()
      .set('fromAccountId', fromAccountId.toString())
      .set('toAccountId', toAccountId.toString())
      .set('amount', amount.toString());
    return this.http.post<string>(`${this.base}/transfer`, null, { params });
  }

  getTransactions(accountId: number, type?: string): Observable<Transaction[]> {
    let url = `${this.base}/${accountId}/transactions/filter`;
    if (type) {
      const params = new HttpParams().set('type', type);
      return this.http.get<Transaction[]>(url, { params });
    }
    return this.http.get<Transaction[]>(url);
  }

  getStatistics(accountId: number): Observable<WalletStatisticsDTO> {
    return this.http.get<WalletStatisticsDTO>(`${this.base}/${accountId}/statistics`);
  }

  forecast(accountId: number, days = 30): Observable<WalletForecastDTO> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<WalletForecastDTO>(`${this.base}/${accountId}/forecast`, { params });
  }

  getAccountTypeAdvice(accountId: number): Observable<AccountTypeAdviceDTO> {
    return this.http.get<AccountTypeAdviceDTO>(`${this.base}/${accountId}/account-type-advice`);
  }
}
