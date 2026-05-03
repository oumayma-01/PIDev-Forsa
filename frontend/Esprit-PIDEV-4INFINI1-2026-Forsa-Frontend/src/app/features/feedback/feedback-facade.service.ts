import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { ComplaintService } from '../../core/data/complaint.service';
import { FeedbackService } from '../../core/data/feedback.service';
import { ResponseService } from '../../core/data/response.service';
import {
  ComplaintBackend,
  ComplaintResponse,
  Feedback,
} from '../../core/models/forsa.models';

@Injectable({ providedIn: 'root' })
export class FeedbackFacadeService {
  private readonly complaintService = inject(ComplaintService);
  private readonly feedbackService = inject(FeedbackService);
  private readonly responseService = inject(ResponseService);

  getAllComplaints(): Observable<ComplaintBackend[]> {
    return this.complaintService.getAll().pipe(map((data: any) => this.asList<ComplaintBackend>(data)));
  }

  getMyComplaints(): Observable<ComplaintBackend[]> {
    return this.complaintService.getMyComplaints().pipe(map((data: any) => this.asList<ComplaintBackend>(data)));
  }

  getComplaintById(id: number): Observable<ComplaintBackend> {
    return this.complaintService.getById(id).pipe(map((data: any) => this.unwrap<ComplaintBackend>(data) as ComplaintBackend));
  }

  getMyFeedbacks(): Observable<Feedback[]> {
    return this.feedbackService.getMyFeedbacks().pipe(map((data: any) => this.asList<Feedback>(data)));
  }

  getAllFeedbacks(): Observable<Feedback[]> {
    return this.feedbackService.getAll().pipe(map((data: any) => this.asList<Feedback>(data)));
  }

  getAllResponses(): Observable<ComplaintResponse[]> {
    return this.responseService.getAll().pipe(map((data: any) => this.asList<ComplaintResponse>(data)));
  }

  deleteComplaint(id: number): Observable<void> {
    return this.complaintService.delete(id);
  }

  closeComplaint(id: number): Observable<void> {
    return this.complaintService.close(id);
  }

  deleteFeedback(id: number): Observable<void> {
    return this.feedbackService.delete(id);
  }

  getAIResponse(complaintId: number): Observable<{ response: string }> {
    return this.complaintService.getAIResponse(complaintId);
  }

  getAIFullReport(): Observable<any> {
    return this.complaintService.getAIFullReport().pipe(map((data: any) => this.unwrap<any>(data)));
  }

  getCreditEligibility(complaintId: number, requiredScore?: number): Observable<any> {
    return this.complaintService.getCreditEligibility(complaintId, requiredScore).pipe(map((data: any) => this.unwrap<any>(data)));
  }

  getFinancialImpactScore(complaintId: number): Observable<any> {
    return this.complaintService.getFinancialImpactScore(complaintId).pipe(map((data: any) => this.unwrap<any>(data)));
  }

  getResponsesSummaryReport(): Observable<any> {
    return this.responseService.getSummaryReport().pipe(map((data: any) => this.unwrap<any>(data)));
  }

  getFeedbackTrends(months: number = 6): Observable<any[]> {
    return this.feedbackService.getTrendsReport(months).pipe(map((data: any) => this.asList<any>(data)));
  }

  private unwrap<T>(data: any): T | null {
    if (data == null) return null;
    return (data?.data ?? data?.result ?? data) as T;
  }

  private asList<T>(data: any): T[] {
    const payload = data?.data ?? data?.result ?? data?.content ?? data;
    if (Array.isArray(payload)) return payload as T[];
    if (payload && typeof payload === 'object') {
      const firstArray = Object.values(payload).find((v) => Array.isArray(v));
      return (firstArray as T[]) ?? [];
    }
    return [];
  }
}
