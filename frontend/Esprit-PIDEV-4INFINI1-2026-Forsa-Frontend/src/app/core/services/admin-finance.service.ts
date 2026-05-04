import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { AdminFinancialDashboard } from '../models/admin-finance.model';

@Injectable({ providedIn: 'root' })
export class AdminFinanceService {
  private readonly http = inject(HttpClient);

  getOverview(): Observable<AdminFinancialDashboard> {
    return this.http.get<AdminFinancialDashboard>(`${environment.apiBaseUrl}/dashboard/admin-financial/overview`);
  }

  seedDemoData(monthsBack = 12, recordsPerMonth = 18): Observable<Record<string, unknown>> {
    const params = new HttpParams()
      .set('monthsBack', String(monthsBack))
      .set('recordsPerMonth', String(recordsPerMonth));

    return this.http.post<Record<string, unknown>>(`${environment.apiBaseUrl}/dashboard/admin-seed/financial-insurance`, null, { params });
  }
}
