import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ComplaintBackend, Category, Priority, ComplaintStatus } from '../models/forsa.models';

@Injectable({ providedIn: 'root' })
export class ComplaintService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/complaints`;

  getAll(): Observable<ComplaintBackend[]> {
    return this.http.get<ComplaintBackend[]>(`${this.baseUrl}/retrieve-all-complaints`);
  }

  getMyComplaints(): Observable<ComplaintBackend[]> {
    console.log('=== CALLING /my-complaints ===');
    console.log('URL:', `${this.baseUrl}/my-complaints`);
    console.log('TOKEN:', (localStorage.getItem('forsa_access_token')?.substring(0, 20) ?? 'null') + '...');
    return this.http.get<ComplaintBackend[]>(`${this.baseUrl}/my-complaints`, { headers: this.headers() });
  }

  getById(id: number): Observable<ComplaintBackend> {
    return this.http.get<ComplaintBackend>(`${this.baseUrl}/retrieve-complaint/${id}`);
  }

  add(complaint: ComplaintBackend): Observable<ComplaintBackend> {
    return this.http.post<ComplaintBackend>(`${this.baseUrl}/add-complaint`, complaint);
  }

  addWithAI(complaint: ComplaintBackend): Observable<ComplaintBackend> {
    return this.http.post<ComplaintBackend>(`${this.baseUrl}/add-complaint-ai`, complaint);
  }

  update(complaint: ComplaintBackend): Observable<ComplaintBackend> {
    return this.http.put<ComplaintBackend>(`${this.baseUrl}/modify-complaint`, complaint);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/remove-complaint/${id}`);
  }

  close(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/close`, {});
  }

  getAIResponse(id: number): Observable<{ response: string }> {
    return this.http.get<{ response: string }>(`${this.baseUrl}/${id}/ai-response`);
  }

  getAIFullReport(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/ai-full-report`);
  }

  getSummaryReport(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/summary-report`);
  }

  getTrendsLastMonths(months: number = 6): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/trends-last-months?months=${months}`);
  }

  getStatsByCategory(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats-by-category`);
  }

  getStatsByPriority(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/stats-by-priority`);
  }

  addResponse(id: number, message: string, responderRole: string, responderName: string): Observable<any> {
    return this.http.post<any>(`${this.baseUrl}/${id}/responses`, {
      message,
      responderRole,
      responderName
    });
  }

  assignToUser(complaintId: number, userId: number): Observable<ComplaintBackend> {
    return this.http.post<ComplaintBackend>(`${this.baseUrl}/${complaintId}/assign/${userId}`, {});
  }

  private headers(): HttpHeaders {
    const token = localStorage.getItem('forsa_access_token') ?? localStorage.getItem('token');
    return token ? new HttpHeaders({ Authorization: `Bearer ${token}` }) : new HttpHeaders();
  }
}
