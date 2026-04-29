import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  Account,
  Activity,
  Transaction,
  WalletStatisticsDTO,
  WalletForecastDTO,
  AccountTypeAdviceDTO,
  AdaptiveInterestResultDTO,
  BankVaultDTO,
} from '../models/wallet.models';

@Injectable({ providedIn: 'root' })
export class AccountService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/accounts`;

  // ── Account CRUD ─────────────────────────────────────────────────────────────

  createAccount(ownerId: number, type: 'SAVINGS' | 'INVESTMENT', holderName?: string): Observable<Account> {
    let params = new HttpParams().set('ownerId', ownerId.toString()).set('type', type);
    if (holderName) params = params.set('holderName', holderName);
    return this.http.post<Account>(`${this.base}/create`, null, { params });
  }

  getAccount(id: number): Observable<Account> {
    return this.http.get<Account>(`${this.base}/${id}`);
  }

  getAllAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/all`);
  }

  getAccountsByOwner(ownerId: number): Observable<Account[]> {
    return this.http.get<Account[]>(`${this.base}/owner/${ownerId}`);
  }

  deleteAccount(accountId: number): Observable<string> {
    return this.http.delete<string>(`${this.base}/${accountId}`);
  }

  updateAccountStatus(accountId: number, status: 'ACTIVE' | 'BLOCKED'): Observable<Account> {
    const params = new HttpParams().set('status', status);
    return this.http.put<Account>(`${this.base}/${accountId}/status`, null, { params });
  }

  // ── Operations ───────────────────────────────────────────────────────────────

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

  applyMonthlyInterest(): Observable<string> {
    return this.http.post<string>(`${this.base}/apply-interest`, null);
  }

  // ── Queries ──────────────────────────────────────────────────────────────────

  getStatistics(accountId: number): Observable<WalletStatisticsDTO> {
    return this.http.get<WalletStatisticsDTO>(`${this.base}/${accountId}/statistics`);
  }

  getTransactions(accountId: number, type?: string): Observable<Transaction[]> {
    const url = `${this.base}/${accountId}/transactions/filter`;
    if (type) {
      return this.http.get<Transaction[]>(url, { params: new HttpParams().set('type', type) });
    }
    return this.http.get<Transaction[]>(url);
  }

  getActivities(accountId: number): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${this.base}/${accountId}/activities`);
  }

  getBankVault(): Observable<BankVaultDTO> {
    return this.http.get<BankVaultDTO>(`${this.base}/vault`);
  }

  // ── AI ───────────────────────────────────────────────────────────────────────

  forecast(accountId: number, days = 30): Observable<WalletForecastDTO> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<WalletForecastDTO>(`${this.base}/${accountId}/forecast`, { params });
  }

  getAccountTypeAdvice(accountId: number): Observable<AccountTypeAdviceDTO> {
    return this.http.get<AccountTypeAdviceDTO>(`${this.base}/${accountId}/account-type-advice`);
  }

  applyAdaptiveInterest(accountId: number): Observable<AdaptiveInterestResultDTO> {
    return this.http.post<AdaptiveInterestResultDTO>(`${this.base}/${accountId}/adaptive-interest`, null);
  }
}
