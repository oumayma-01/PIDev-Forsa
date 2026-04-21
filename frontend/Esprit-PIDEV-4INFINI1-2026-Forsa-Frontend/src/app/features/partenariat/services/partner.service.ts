import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { Partner, PartnerType } from '../../../core/models/forsa.models';

/** Service HTTP pour le module Partenariat — branché sur le backend Spring Boot. */
@Injectable({ providedIn: 'root' })
export class PartnerService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiBaseUrl}/partners`;

  /** Récupère tous les partenaires. */
  getAllPartners(): Observable<Partner[]> {
    return this.http.get<Partner[]>(this.apiUrl);
  }

  /** Récupère un partenaire par son identifiant. */
  getPartnerById(id: number): Observable<Partner> {
    return this.http.get<Partner>(`${this.apiUrl}/${id}`);
  }

  /** Crée un nouveau partenaire (rôle ADMIN requis). */
  createPartner(partner: Partial<Partner>): Observable<Partner> {
    return this.http.post<Partner>(this.apiUrl, partner);
  }

  /** Met à jour un partenaire existant (rôle ADMIN requis). */
  updatePartner(id: number, partner: Partial<Partner>): Observable<Partner> {
    return this.http.put<Partner>(`${this.apiUrl}/${id}`, partner);
  }

  /** Supprime un partenaire (rôle ADMIN requis). */
  deletePartner(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /** Récupère uniquement les partenaires actifs. */
  getActivePartners(): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}/active`);
  }

  /** Récupère les partenaires d'un type donné. */
  getPartnersByType(type: PartnerType): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}/type/${type}`);
  }
}
