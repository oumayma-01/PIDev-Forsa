import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { InsurancePolicy, InsurancePolicyApplicationDTO } from '../models/insurance.models';
import { PolicyStatus } from '../enums/insurance.enums';

@Injectable({ providedIn: 'root' })
export class InsurancePolicyService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/insurance-policy`;

  getAll(): Observable<InsurancePolicy[]> {
    return this.http.get<InsurancePolicy[]>(`${this.base}/retrieve-all-insurance-policies`);
  }

  getMyPolicies(): Observable<InsurancePolicy[]> {
    return this.http.get<InsurancePolicy[]>(`${this.base}/my-policies`);
  }

  getById(id: number): Observable<InsurancePolicy> {
    return this.http.get<InsurancePolicy>(`${this.base}/retrieve-insurance-policy/${id}`);
  }

  clientApply(application: InsurancePolicyApplicationDTO): Observable<InsurancePolicy> {
    return this.http.post<InsurancePolicy>(`${this.base}/client-apply`, application);
  }

  agentReview(
    policyId: number,
    status: PolicyStatus,
    approvedCoverage?: number,
    notes?: string,
  ): Observable<InsurancePolicy> {
    let params = new HttpParams().set('status', status);
    if (approvedCoverage != null) params = params.set('approvedCoverage', approvedCoverage);
    if (notes) params = params.set('notes', notes);
    return this.http.put<InsurancePolicy>(`${this.base}/agent-review/${policyId}`, null, { params });
  }

  create(policy: InsurancePolicy): Observable<InsurancePolicy> {
    return this.http.post<InsurancePolicy>(`${this.base}/add-insurance-policy`, policy);
  }

  update(policy: InsurancePolicy): Observable<InsurancePolicy> {
    return this.http.put<InsurancePolicy>(`${this.base}/modify-insurance-policy`, policy);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/remove-insurance-policy/${id}`);
  }

  downloadAmortizationPdf(policyId: number): Observable<Blob> {
    const url = `${environment.apiBaseUrl}/policy-pdf/amortization/download/${policyId}`;
    return this.http.get(url, { responseType: 'blob' });
  }

  viewAmortizationPdf(policyId: number): Observable<Blob> {
    const url = `${environment.apiBaseUrl}/policy-pdf/amortization/view/${policyId}`;
    return this.http.get(url, { responseType: 'blob' });
  }

  downloadPolicyContract(policyId: number): Observable<Blob> {
    const url = `${environment.apiBaseUrl}/policy-pdf/download/${policyId}`;
    return this.http.get(url, { responseType: 'blob' });
  }

  viewPolicyContract(policyId: number): Observable<Blob> {
    const url = `${environment.apiBaseUrl}/policy-pdf/view/${policyId}`;
    return this.http.get(url, { responseType: 'blob' });
  }

  signPolicy(policyId: number, signature: string): Observable<InsurancePolicy> {
    return this.http.put<InsurancePolicy>(`${this.base}/sign-policy/${policyId}`, signature);
  }
}
