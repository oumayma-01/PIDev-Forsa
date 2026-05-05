import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type {
  AIScoreRequest,
  AIScoreResponse,
  AIScoreDto,
  AIScoreSummaryDto,
  ScoreStatusResponse,
} from '../../../core/models/forsa.models';

/** Result of document OCR verification (Python). */
export interface OcrResult {
  document_type: string;
  verified: boolean;
  // CIN
  cin_number?: string | null;
  name?: string | null;
  // STEG / SONEDE
  paid_on_time?: boolean;
  status?: string | null;
  amount?: number | null;
  // SALARY
  salary?: number | null;
  employer?: string | null;
  // Meta
  ocr_method?: string;
  error?: string;
}

@Injectable({ providedIn: 'root' })
export class AiScoreService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiBaseUrl}/ai-score`;

  /** Check whether a client already has a score — does NOT auto-create one. */
  getScoreStatus(clientId: number): Observable<ScoreStatusResponse> {
    return this.http.get<ScoreStatusResponse>(`${this.apiUrl}/status/${clientId}`);
  }

  /**
   * First-time score for a new client.
   * Stores OCR salary, activates boosters if bills are paid, then calculates.
   * POST /api/ai-score/first-score/{clientId}
   */
  submitFirstScore(
    clientId: number,
    salary: number,
    stegPaidOnTime: boolean,
    sondePaidOnTime: boolean,
  ): Observable<AIScoreDto> {
    return this.http.post<AIScoreDto>(`${this.apiUrl}/first-score/${clientId}`, {
      salary,
      stegPaidOnTime,
      sondePaidOnTime,
    });
  }

  /**
   * Launch AI score calculation (calls Python directly, returns rich response).
   * POST /api/ai-score/calculate-body/{clientId}
   */
  calculateScore(clientId: number, request: Omit<AIScoreRequest, 'clientId'>): Observable<AIScoreResponse> {
    return this.http.post<AIScoreResponse>(
      `${this.apiUrl}/calculate-body/${clientId}`,
      request,
    );
  }

  /**
   * Verify a document via OCR.
   * POST /api/ai-score/verify-document (multipart)
   * document_type: CIN | STEG | SONEDE | SALARY
   */
  verifyDocument(file: File, documentType: 'CIN' | 'STEG' | 'SONEDE' | 'SALARY'): Observable<OcrResult> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('document_type', documentType);
    return this.http.post<OcrResult>(`${this.apiUrl}/verify-document`, formData);
  }

  /** Retrieve the last saved score for a client (auto-creates if absent). */
  getCurrentScore(clientId: number): Observable<AIScoreDto> {
    return this.http.get<AIScoreDto>(`${this.apiUrl}/current/${clientId}`);
  }

  /** Recalculate and persist the AI score. */
  recalculateScore(clientId: number): Observable<AIScoreDto> {
    return this.http.post<AIScoreDto>(`${this.apiUrl}/recalculate/${clientId}`, {});
  }

  /** Activate a STEG or SONEDE booster and return the updated score. */
  activateBooster(clientId: number, type: 'STEG' | 'SONEDE'): Observable<AIScoreDto> {
    return this.http.post<AIScoreDto>(
      `${this.apiUrl}/boosters/${clientId}/activate?type=${encodeURIComponent(type)}`,
      {},
    );
  }

  /** Admin list — GET /api/ai-score/all */
  getAllSummaries(): Observable<AIScoreSummaryDto[]> {
    return this.http.get<AIScoreSummaryDto[]>(`${this.apiUrl}/all`);
  }

  /** Health check. */
  checkHealth(): Observable<{ status: string; service: string }> {
    return this.http.get<{ status: string; service: string }>(`${this.apiUrl}/health`);
  }
}
