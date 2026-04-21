import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { ForsaRoleName } from '../models/user-admin.model';
import type { RoleAccessCatalogEntry, RoleAccessGrant, RoleAccessUpdateRequest } from '../models/role-access.model';

@Injectable({ providedIn: 'root' })
export class RoleAccessService {
  private readonly http = inject(HttpClient);

  getCatalog(): Observable<RoleAccessCatalogEntry[]> {
    return this.http.get<RoleAccessCatalogEntry[]>(`${environment.apiBaseUrl}/role/access/catalog`);
  }

  getGrantsForRole(role: ForsaRoleName): Observable<RoleAccessGrant[]> {
    return this.http.get<RoleAccessGrant[]>(`${environment.apiBaseUrl}/role/access/role/${role}`);
  }

  updateRoleAccess(role: ForsaRoleName, body: RoleAccessUpdateRequest): Observable<RoleAccessGrant[]> {
    return this.http.put<RoleAccessGrant[]>(
      `${environment.apiBaseUrl}/role/access/role/${role}`,
      body,
    );
  }
}
