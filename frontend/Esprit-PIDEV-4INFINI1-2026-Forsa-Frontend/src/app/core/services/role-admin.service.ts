import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { RoleWithStats } from '../models/role-admin.model';

@Injectable({ providedIn: 'root' })
export class RoleAdminService {
  private readonly http = inject(HttpClient);

  listRoles(): Observable<RoleWithStats[]> {
    return this.http.get<RoleWithStats[]>(`${environment.apiBaseUrl}/role/with-stats`);
  }
}
