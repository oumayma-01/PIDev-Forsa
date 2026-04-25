import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { ClaimsDashboardDTO } from '../models/claims-dashboard.models';

@Injectable({ providedIn: 'root' })
export class ClaimsDashboardService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/claims-dashboard`;

  getAnalytics(): Observable<ClaimsDashboardDTO> {
    return this.http.get<ClaimsDashboardDTO>(`${this.base}/analytics`);
  }
}
