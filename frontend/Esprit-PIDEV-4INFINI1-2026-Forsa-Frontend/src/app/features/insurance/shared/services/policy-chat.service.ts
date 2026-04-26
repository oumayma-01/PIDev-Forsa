import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';

export interface ChatMessage {
  id?: number;
  policyId: number;
  role: 'user' | 'assistant';
  content: string;
  timestamp: string;
}

export interface ChatResponseDTO {
  reply: string;
  timestamp: string;
}

@Injectable({
  providedIn: 'root'
})
export class PolicyChatService {
  private apiUrl = `${environment.apiBaseUrl}/insurance-chat`;

  constructor(private http: HttpClient) {}

  sendMessage(policyId: number, message: string): Observable<ChatResponseDTO> {
    return this.http.post<ChatResponseDTO>(`${this.apiUrl}/send`, { policyId, message });
  }

  getHistory(policyId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/history/${policyId}`);
  }
}
