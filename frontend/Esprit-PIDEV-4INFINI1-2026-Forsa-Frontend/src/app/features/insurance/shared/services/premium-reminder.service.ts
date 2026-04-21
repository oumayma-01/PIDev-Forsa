import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class PremiumReminderService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/premium-reminder`;

  sendUpcomingReminders(): Observable<string> {
    return this.http.post<string>(`${this.base}/send-upcoming-reminders`, {});
  }

  markOverduePayments(): Observable<string> {
    return this.http.post<string>(`${this.base}/mark-overdue`, {});
  }

  runFullCheck(): Observable<string> {
    return this.http.post<string>(`${this.base}/run-full-check`, {});
  }
}
