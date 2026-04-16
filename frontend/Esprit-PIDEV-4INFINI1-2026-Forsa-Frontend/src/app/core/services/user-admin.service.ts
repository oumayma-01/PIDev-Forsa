import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { ManagedUser } from '../models/user-admin.model';
import type { MessageResponse, SignupPayload } from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private readonly http = inject(HttpClient);

  listUsers(): Observable<ManagedUser[]> {
    return this.http.get<ManagedUser[]>(`${environment.apiBaseUrl}/user/all`);
  }

  updateUser(id: number, payload: SignupPayload): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`${environment.apiBaseUrl}/user/update/${id}`, payload);
  }

  deleteUser(id: number): Observable<string> {
    return this.http.delete(`${environment.apiBaseUrl}/user/delete/${id}`, {
      responseType: 'text',
    });
  }

  setUserActive(id: number, active: boolean): Observable<MessageResponse> {
    const params = new HttpParams().set('active', String(active));
    return this.http.patch<MessageResponse>(`${environment.apiBaseUrl}/user/active/${id}`, null, {
      params,
    });
  }

  createAgent(payload: SignupPayload): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiBaseUrl}/user/agent`, payload);
  }
}
