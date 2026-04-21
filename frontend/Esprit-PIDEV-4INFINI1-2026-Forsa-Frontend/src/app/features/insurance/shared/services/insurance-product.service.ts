import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { InsuranceProduct } from '../models/insurance.models';

@Injectable({ providedIn: 'root' })
export class InsuranceProductService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/insurance-product`;

  getAll(): Observable<InsuranceProduct[]> {
    return this.http.get<InsuranceProduct[]>(`${this.base}/retrieve-all-insurance-products`);
  }

  getById(id: number): Observable<InsuranceProduct> {
    return this.http.get<InsuranceProduct>(`${this.base}/retrieve-insurance-product/${id}`);
  }

  create(product: InsuranceProduct): Observable<InsuranceProduct> {
    return this.http.post<InsuranceProduct>(`${this.base}/add-insurance-product`, product);
  }

  update(product: InsuranceProduct): Observable<InsuranceProduct> {
    return this.http.put<InsuranceProduct>(`${this.base}/modify-insurance-product`, product);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/remove-insurance-product/${id}`);
  }

  affectPolicies(productId: number, policyIds: number[]): Observable<InsuranceProduct> {
    return this.http.put<InsuranceProduct>(`${this.base}/affect-policies/${productId}`, policyIds);
  }
}
