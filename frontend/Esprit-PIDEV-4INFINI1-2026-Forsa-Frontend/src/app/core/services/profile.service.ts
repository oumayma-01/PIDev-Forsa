import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { CurrentUser, MessageResponse } from '../models/auth.model';

export interface ChangePasswordPayload {
  /** Omitted or empty for Google-only accounts (first-time password). */
  currentPassword?: string;
  newPassword: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);

  getProfile(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${environment.apiBaseUrl}/profile/me`);
  }

  updateProfile(payload: { username: string; email: string }): Observable<CurrentUser> {
    return this.http.put<CurrentUser>(`${environment.apiBaseUrl}/profile/me`, payload);
  }

  changePassword(payload: ChangePasswordPayload): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`${environment.apiBaseUrl}/profile/me/password`, payload);
  }

  uploadAvatar(file: File): Observable<CurrentUser> {
    const body = new FormData();
    body.append('file', file);
    return this.http.post<CurrentUser>(`${environment.apiBaseUrl}/profile/me/avatar`, body);
  }

  deleteAvatar(): Observable<CurrentUser> {
    return this.http.delete<CurrentUser>(`${environment.apiBaseUrl}/profile/me/avatar`);
  }

  getAvatarBlob(): Observable<Blob> {
    return this.http.get(`${environment.apiBaseUrl}/profile/me/avatar`, {
      responseType: 'blob',
    });
  }
}
