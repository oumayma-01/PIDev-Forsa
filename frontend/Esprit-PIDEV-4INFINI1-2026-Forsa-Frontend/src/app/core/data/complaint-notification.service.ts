import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ComplaintNotification {
  id: number;
  title: string;
  message: string;
  isRead: boolean;
  createdAt: string;
  type: string;
  complaint?: { id: number; subject: string };
}

@Injectable({ providedIn: 'root' })
export class ComplaintNotificationService {
  private baseUrl = `${environment.apiBaseUrl}/complaint-notifications`;

  constructor(private http: HttpClient) {}

  private headers(): HttpHeaders {
    const token = localStorage.getItem('forsa_access_token');
    return new HttpHeaders({
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    });
  }

  getMyNotifications(): Observable<ComplaintNotification[]> {
    return this.http.get<ComplaintNotification[]>(
      `${this.baseUrl}/my-notifications`,
      { headers: this.headers() },
    );
  }

  getUnreadCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(
      `${this.baseUrl}/unread-count`,
      { headers: this.headers() },
    );
  }

  markAsRead(id: number): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/${id}/read`,
      {},
      { headers: this.headers() },
    );
  }

  markAllAsRead(): Observable<void> {
    return this.http.put<void>(
      `${this.baseUrl}/mark-all-read`,
      {},
      { headers: this.headers() },
    );
  }
}
