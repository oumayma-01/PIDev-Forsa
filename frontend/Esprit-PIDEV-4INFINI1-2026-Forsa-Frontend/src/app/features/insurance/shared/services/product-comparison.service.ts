import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { InsuranceProductComparisonDTO } from '../models/insurance.models';

@Injectable({
  providedIn: 'root'
})
export class ProductComparisonService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiBaseUrl}/product-comparison`;

  getAllProductsForComparison(): Observable<InsuranceProductComparisonDTO[]> {
    return this.http.get<InsuranceProductComparisonDTO[]>(`${this.base}/products`);
  }

  compareProducts(productIds: number[]): Observable<any> {
    const params = new HttpParams().set('productIds', productIds.join(','));
    return this.http.get<any>(`${this.base}/compare`, { params });
  }

  downloadComparisonPdf(productIds: number[]): Observable<Blob> {
    const params = new HttpParams().set('productIds', productIds.join(','));
    return this.http.get(`${this.base}/download-pdf`, { params, responseType: 'blob' });
  }
}
