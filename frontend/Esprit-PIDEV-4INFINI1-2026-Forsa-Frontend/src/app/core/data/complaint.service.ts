import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ComplaintBackend, Category, Priority, ComplaintStatus } from '../models/forsa.models';

export interface ComplaintCreditEligibility {
  complaintId: number;
  clientId: number | null;
  currentScore: number;
  requiredScore: number;
  gap: number;
  eligible: boolean;
  fallbackUsed: boolean;
}

export interface ComplaintFinancialImpact {
  complaintId: number;
  complaintAmount: number;
  amountSource: string;
  priority: string;
  daysSinceCreation: number;
  financialImpactScore: number;
}

@Injectable({ providedIn: 'root' })
export class ComplaintService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/complaints`;

  getAll(): Observable<ComplaintBackend[]> {
    return this.http.get<ComplaintBackend[]>(`${this.baseUrl}/retrieve-all-complaints`);
  }

  getMyComplaints(): Observable<ComplaintBackend[]> {
    return this.http.get<ComplaintBackend[]>(`${this.baseUrl}/my-complaints`);
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

  getCreditEligibility(complaintId: number, requiredScore: number = 70): Observable<ComplaintCreditEligibility> {
    return this.http.get<ComplaintCreditEligibility>(
      `${this.baseUrl}/${complaintId}/financial/credit-eligibility?requiredScore=${requiredScore}`
    );
  }

  getFinancialImpactScore(complaintId: number): Observable<ComplaintFinancialImpact> {
    return this.http.get<ComplaintFinancialImpact>(`${this.baseUrl}/${complaintId}/financial/impact-score`);
  }
}
