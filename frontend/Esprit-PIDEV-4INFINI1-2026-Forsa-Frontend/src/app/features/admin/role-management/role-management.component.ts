import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import type { ForsaRoleName } from '../../../core/models/user-admin.model';
import type { RoleAccessCatalogEntry, RoleAccessGrant } from '../../../core/models/role-access.model';
import type { RoleWithStats } from '../../../core/models/role-admin.model';
import { RoleAccessService } from '../../../core/services/role-access.service';
import { RoleAdminService } from '../../../core/services/role-admin.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

interface AccessRow {
  resourceCode: string;
  pathPattern: string;
  title: string;
  description: string;
  permitted: boolean;
}

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './role-management.component.html',
  styleUrl: './role-management.component.css',
})
export class RoleManagementComponent implements OnInit {
  private readonly rolesApi = inject(RoleAdminService);
  private readonly accessApi = inject(RoleAccessService);

  readonly roles = signal<RoleWithStats[]>([]);
  readonly loading = signal(false);
  readonly banner = signal<{ tone: 'ok' | 'err'; text: string } | null>(null);

  readonly accessRole = signal<ForsaRoleName>('AGENT');
  readonly accessRows = signal<AccessRow[]>([]);
  readonly accessLoading = signal(false);
  readonly accessSaving = signal(false);
  readonly accessBanner = signal<{ tone: 'ok' | 'err'; text: string } | null>(null);

  readonly accessRoleOptions: ForsaRoleName[] = ['CLIENT', 'AGENT', 'ADMIN'];

  ngOnInit(): void {
    this.refresh();
    this.loadAccessMatrix();
  }

  refresh(): void {
    this.loading.set(true);
    this.banner.set(null);
    this.rolesApi.listRoles().subscribe({
      next: (rows) => {
        this.roles.set(rows);
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.banner.set({ tone: 'err', text: e.error?.message ?? 'Could not load roles.' });
      },
    });
  }

  onAccessRoleChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as ForsaRoleName;
    this.accessRole.set(value);
    this.loadAccessMatrix();
  }

  loadAccessMatrix(): void {
    this.accessLoading.set(true);
    this.accessBanner.set(null);
    const role = this.accessRole();
    forkJoin({
      catalog: this.accessApi.getCatalog(),
      grants: this.accessApi.getGrantsForRole(role),
    }).subscribe({
      next: ({ catalog, grants }) => {
        this.accessRows.set(this.mergeAccessRows(catalog, grants));
        this.accessLoading.set(false);
      },
      error: (e) => {
        this.accessLoading.set(false);
        this.accessBanner.set({
          tone: 'err',
          text: e.error?.message ?? 'Impossible de charger les accès API.',
        });
      },
    });
  }

  onToggleAccess(row: AccessRow, ev: Event): void {
    const t = ev.target;
    if (t instanceof HTMLInputElement) {
      this.toggleAccess(row.resourceCode, t.checked);
    }
  }

  private toggleAccess(code: string, checked: boolean): void {
    this.accessRows.update((rows) =>
      rows.map((r) => (r.resourceCode === code ? { ...r, permitted: checked } : r)),
    );
  }

  saveAccessMatrix(): void {
    const role = this.accessRole();
    this.accessSaving.set(true);
    this.accessBanner.set(null);
    const grants: RoleAccessGrant[] = this.accessRows().map((r) => ({
      resourceCode: r.resourceCode,
      permitted: r.permitted,
    }));
    this.accessApi.updateRoleAccess(role, { grants }).subscribe({
      next: (updated) => {
        this.accessRows.update((rows) =>
          rows.map((r) => {
            const g = updated.find((x) => x.resourceCode === r.resourceCode);
            return g ? { ...r, permitted: g.permitted } : r;
          }),
        );
        this.accessSaving.set(false);
        this.accessBanner.set({ tone: 'ok', text: 'Accès enregistrés.' });
      },
      error: (e) => {
        this.accessSaving.set(false);
        this.accessBanner.set({
          tone: 'err',
          text: e.error?.message ?? 'Échec de la mise à jour des accès.',
        });
      },
    });
  }

  private mergeAccessRows(catalog: RoleAccessCatalogEntry[], grants: RoleAccessGrant[]): AccessRow[] {
    const g = new Map(grants.map((x) => [x.resourceCode, x.permitted]));
    return catalog.map((c) => ({
      resourceCode: c.code,
      pathPattern: c.pathPattern,
      title: c.title,
      description: c.description,
      permitted: g.get(c.code) ?? false,
    }));
  }

  roleTone(name: string): 'success' | 'warning' | 'danger' | 'info' {
    const r = name as ForsaRoleName;
    switch (r) {
      case 'ADMIN':
        return 'danger';
      case 'AGENT':
        return 'info';
      default:
        return 'success';
    }
  }
}
