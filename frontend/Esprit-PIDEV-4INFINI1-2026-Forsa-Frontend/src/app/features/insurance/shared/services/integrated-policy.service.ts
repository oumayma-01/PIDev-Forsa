import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { InsurancePolicy, InsurancePolicyApplicationDTO } from '../models/insurance.models';

@Injectable({
  providedIn: 'root'
})
export class IntegratedPolicyService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/insurance-policy`;

  applyForInsurance(application: InsurancePolicyApplicationDTO): Observable<InsurancePolicy> {
    return this.http.post<InsurancePolicy>(`${this.base}/client-apply`, application);
  }
}
