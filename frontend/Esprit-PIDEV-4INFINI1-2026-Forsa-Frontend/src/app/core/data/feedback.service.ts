import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Feedback } from '../models/forsa.models';

export interface FeedbackAvgByGroup {
  group: string;
  avgRating: number;
}

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/feedbacks`;

  getAll(): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.baseUrl}/retrieve-all-feedbacks`);
  }

  getMyFeedbacks(): Observable<Feedback[]> {
    return this.http.get<Feedback[]>(`${this.baseUrl}/my-feedbacks`);
  }

  getById(id: number): Observable<Feedback> {
    return this.http.get<Feedback>(`${this.baseUrl}/retrieve-feedback/${id}`);
  }

  add(feedback: Feedback): Observable<Feedback> {
    return this.http.post<Feedback>(`${this.baseUrl}/add-feedback`, feedback);
  }

  addWithAI(feedback: Feedback): Observable<Feedback> {
    return this.http.post<Feedback>(`${this.baseUrl}/add-feedback-ai`, feedback);
  }

  update(feedback: Feedback): Observable<Feedback> {
    return this.http.put<Feedback>(`${this.baseUrl}/modify-feedback`, feedback);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/remove-feedback/${id}`);
  }

  getSummary(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/report/summary`);
  }

  getTrends(months: number = 6): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/report/trends?months=${months}`);
  }

  getAvgRatingByCategory(): Observable<FeedbackAvgByGroup[]> {
    return this.http.get<FeedbackAvgByGroup[]>(`${this.baseUrl}/report/avg-rating-by-category`);
  }

  // Backward-compatible aliases used by existing components.
  getSummaryReport(): Observable<any> {
    return this.getSummary();
  }

  getTrendsReport(months: number = 6): Observable<any> {
    return this.getTrends(months);
  }
}
