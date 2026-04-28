import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { InsuranceClaim, ClaimTemplate } from '../models/insurance.models';

@Injectable({ providedIn: 'root' })
export class InsuranceClaimService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/insurance-claim`;

  getAll(): Observable<InsuranceClaim[]> {
    return this.http.get<InsuranceClaim[]>(`${this.base}/retrieve-all-insurance-claims`);
  }

  getMyClaims(): Observable<InsuranceClaim[]> {
    return this.http.get<InsuranceClaim[]>(`${this.base}/my-claims`);
  }

  getById(id: number): Observable<InsuranceClaim> {
    return this.http.get<InsuranceClaim>(`${this.base}/retrieve-insurance-claim/${id}`);
  }

  create(claim: InsuranceClaim): Observable<InsuranceClaim> {
    return this.http.post<InsuranceClaim>(`${this.base}/add-insurance-claim`, claim);
  }

  update(claim: InsuranceClaim): Observable<InsuranceClaim> {
    return this.http.put<InsuranceClaim>(`${this.base}/modify-insurance-claim`, claim);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/remove-insurance-claim/${id}`);
  }

  uploadAttachment(file: File): Observable<string> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post(`${this.base}/upload-attachment`, formData, { responseType: 'text' });
  }

  getAttachmentUrl(fileName: string): string {
    return `${this.base}/attachments/${fileName}`;
  }

  getClaimTemplate(policyType: string): Observable<ClaimTemplate> {
    return this.http.get<ClaimTemplate>(`${this.base}/template/${policyType}`);
  }
}
