import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ComplaintResponse, ResponseStatus } from '../models/forsa.models';

@Injectable({ providedIn: 'root' })
export class ResponseService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/responses`;

  getAll(): Observable<ComplaintResponse[]> {
    return this.http.get<ComplaintResponse[]>(`${this.baseUrl}/retrieve-all-responses`);
  }

  getById(id: number): Observable<ComplaintResponse> {
    return this.http.get<ComplaintResponse>(`${this.baseUrl}/retrieve-response/${id}`);
  }

  add(response: ComplaintResponse): Observable<ComplaintResponse> {
    return this.http.post<ComplaintResponse>(`${this.baseUrl}/add-response`, response);
  }

  update(response: ComplaintResponse): Observable<ComplaintResponse> {
    return this.http.put<ComplaintResponse>(`${this.baseUrl}/modify-response`, response);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/remove-response/${id}`);
  }

  improveWithAI(id: number): Observable<ComplaintResponse> {
    return this.http.put<ComplaintResponse>(`${this.baseUrl}/improve-response-ai/${id}`, {});
  }

  getSummaryReport(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/report/summary`);
  }
}
