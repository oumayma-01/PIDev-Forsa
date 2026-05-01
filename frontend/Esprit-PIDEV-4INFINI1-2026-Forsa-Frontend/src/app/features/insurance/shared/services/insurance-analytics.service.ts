import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, timeout, retry, catchError, throwError } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { InsuranceOverviewDTO } from '../models/insurance-analytics.models';

@Injectable({ providedIn: 'root' })
export class InsuranceAnalyticsService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/insurance-analytics`;

  getOverview(): Observable<InsuranceOverviewDTO> {
    console.log('AnalyticsService: Requesting overview...');
    return this.http.get<InsuranceOverviewDTO>(`${this.base}/overview`).pipe(
      timeout(20000), // 20s timeout
      retry(1),
      catchError(err => {
        console.error('AnalyticsService: Request failed', err);
        return throwError(() => err);
      })
    );
  }
}
