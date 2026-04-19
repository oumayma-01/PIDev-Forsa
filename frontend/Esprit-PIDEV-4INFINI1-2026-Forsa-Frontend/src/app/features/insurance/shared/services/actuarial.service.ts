import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { 
  InsuranceRiskAssessmentDTO, 
  PremiumCalculationRequestDTO, 
  PremiumCalculationResultDTO, 
  InsuranceAmortizationScheduleDTO,
  InsuranceCompleteQuoteDTO 
} from '../models/insurance.models';

@Injectable({
  providedIn: 'root'
})
export class ActuarialService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/actuarial`;

  assessRisk(riskProfile: InsuranceRiskAssessmentDTO): Observable<InsuranceRiskAssessmentDTO> {
    return this.http.post<InsuranceRiskAssessmentDTO>(`${this.base}/risk-assessment`, riskProfile);
  }

  calculatePremium(request: PremiumCalculationRequestDTO): Observable<PremiumCalculationResultDTO> {
    return this.http.post<PremiumCalculationResultDTO>(`${this.base}/calculate-premium`, request);
  }

  generateAmortizationSchedule(principal: number, annualRate: number, durationMonths: number, paymentFrequency: string): Observable<InsuranceAmortizationScheduleDTO> {
    const params = new HttpParams()
      .set('principal', principal.toString())
      .set('annualRate', annualRate.toString())
      .set('durationMonths', durationMonths.toString())
      .set('paymentFrequency', paymentFrequency);
    return this.http.get<InsuranceAmortizationScheduleDTO>(`${this.base}/amortization-schedule`, { params });
  }

  getCompleteQuote(request: PremiumCalculationRequestDTO): Observable<InsuranceCompleteQuoteDTO> {
    return this.http.post<InsuranceCompleteQuoteDTO>(`${this.base}/complete-quote`, request);
  }
}
