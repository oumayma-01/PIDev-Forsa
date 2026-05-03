import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { AIScoreRequest, AIScoreResponse, AIScoreDto, AIScoreSummaryDto } from '../../../core/models/forsa.models';

/** Résultat de vérification de document (Python). */
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

  /**
   * Lance le calcul du score IA.
   * POST /api/ai-score/calculate-body/{clientId}
   */
  calculateScore(clientId: number, request: Omit<AIScoreRequest, 'clientId'>): Observable<AIScoreResponse> {
    return this.http.post<AIScoreResponse>(
      `${this.apiUrl}/calculate-body/${clientId}`,
      request
    );
  }

  /**
   * Vérifie un document par OCR.
   * POST /api/ai-score/verify-document (multipart)
   * document_type : CIN | STEG | SONEDE | SALARY
   */
  verifyDocument(file: File, documentType: 'CIN' | 'STEG' | 'SONEDE' | 'SALARY'): Observable<OcrResult> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('document_type', documentType);
    return this.http.post<OcrResult>(`${this.apiUrl}/verify-document`, formData);
  }

  /** Récupère le dernier score d'un client. */
  getCurrentScore(clientId: number): Observable<AIScoreDto> {
    return this.http.get<AIScoreDto>(`${this.apiUrl}/current/${clientId}`);
  }

  /** Recalcule le score IA pour un client. */
  recalculateScore(clientId: number): Observable<AIScoreDto> {
    return this.http.post<AIScoreDto>(`${this.apiUrl}/recalculate/${clientId}`, {});
  }

  /** Active un booster STEG ou SONEDE et retourne le score mis à jour. */
  activateBooster(clientId: number, type: 'STEG' | 'SONEDE'): Observable<AIScoreDto> {
    return this.http.post<AIScoreDto>(
      `${this.apiUrl}/boosters/${clientId}/activate?type=${encodeURIComponent(type)}`,
      {},
    );
  }

  /** Liste admin — GET /api/ai-score/all */
  getAllSummaries(): Observable<AIScoreSummaryDto[]> {
    return this.http.get<AIScoreSummaryDto[]>(`${this.apiUrl}/all`);
  }

  /** Vérifie que le controller Spring Boot est actif. */
  checkHealth(): Observable<{ status: string; service: string }> {
    return this.http.get<{ status: string; service: string }>(`${this.apiUrl}/health`);
  }
}
