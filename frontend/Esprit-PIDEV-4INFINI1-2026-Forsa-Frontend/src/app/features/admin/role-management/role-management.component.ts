import { Component, OnInit, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import type { ForsaRoleName } from '../../../core/models/user-admin.model';
import type { RoleAccessCatalogEntry, RoleAccessGrant } from '../../../core/models/role-access.model';
import type { RoleWithStats } from '../../../core/models/role-admin.model';
import { AuthService } from '../../../core/services/auth.service';
import { RoleAccessService } from '../../../core/services/role-access.service';
import { RoleAdminService } from '../../../core/services/role-admin.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

interface NavAccessRow {
  resourceCode: string;
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
  private readonly auth = inject(AuthService);

  readonly roles = signal<RoleWithStats[]>([]);
  readonly loading = signal(false);
  readonly banner = signal<{ tone: 'ok' | 'err'; text: string } | null>(null);

  readonly editOpen = signal(false);
  readonly editingRole = signal<RoleWithStats | null>(null);
  readonly dialogRows = signal<NavAccessRow[]>([]);
  readonly dialogLoading = signal(false);
  readonly dialogSaving = signal(false);
  readonly dialogBanner = signal<{ tone: 'ok' | 'err'; text: string } | null>(null);

  ngOnInit(): void {
    this.refresh();
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

  openEditAccess(role: RoleWithStats): void {
    this.editingRole.set(role);
    this.dialogBanner.set(null);
    this.editOpen.set(true);
    this.loadDialogMatrix();
  }

  closeEdit(): void {
    this.editOpen.set(false);
    this.editingRole.set(null);
    this.dialogRows.set([]);
  }

  loadDialogMatrix(): void {
    const role = this.editingRole();
    if (!role) {
      return;
    }
    this.dialogLoading.set(true);
    this.dialogBanner.set(null);
    const roleName = role.name as ForsaRoleName;
    forkJoin({
      catalog: this.accessApi.getCatalog(),
      grants: this.accessApi.getGrantsForRole(roleName),
    }).subscribe({
      next: ({ catalog, grants }) => {
        this.dialogRows.set(this.mergeRows(catalog, grants));
        this.dialogLoading.set(false);
      },
      error: (e) => {
        this.dialogLoading.set(false);
        this.dialogBanner.set({
          tone: 'err',
          text: e.error?.message ?? 'Could not load access settings.',
        });
      },
    });
  }

  onToggleDialogRow(row: NavAccessRow, ev: Event): void {
    const t = ev.target;
    if (t instanceof HTMLInputElement) {
      this.toggleRow(row.resourceCode, t.checked);
    }
  }

  private toggleRow(code: string, checked: boolean): void {
    this.dialogRows.update((rows) =>
      rows.map((r) => (r.resourceCode === code ? { ...r, permitted: checked } : r)),
    );
  }

  saveDialogAccess(): void {
    const role = this.editingRole();
    if (!role) {
      return;
    }
    this.dialogSaving.set(true);
    this.dialogBanner.set(null);
    const grants: RoleAccessGrant[] = this.dialogRows().map((r) => ({
      resourceCode: r.resourceCode,
      permitted: r.permitted,
    }));
    this.accessApi.updateRoleAccess(role.name as ForsaRoleName, { grants }).subscribe({
      next: (updated) => {
        this.dialogRows.update((rows) =>
          rows.map((r) => {
            const g = updated.find((x) => x.resourceCode === r.resourceCode);
            return g ? { ...r, permitted: g.permitted } : r;
          }),
        );
        this.dialogSaving.set(false);
        this.dialogBanner.set({ tone: 'ok', text: 'Access settings saved.' });
        this.refreshCurrentUserIfSameRole(role.name);
      },
      error: (e) => {
        this.dialogSaving.set(false);
        this.dialogBanner.set({
          tone: 'err',
          text: e.error?.message ?? 'Could not update access settings.',
        });
      },
    });
  }

  /** Met à jour les menus si l’admin modifie les accès de son propre rôle. */
  private refreshCurrentUserIfSameRole(editedRoleName: string): void {
    const u = this.auth.currentUser();
    if (!u?.roles?.length) {
      return;
    }
    const want = `ROLE_${editedRoleName}`;
    if (!u.roles.includes(want)) {
      return;
    }
    this.auth.refreshCurrentUser().subscribe({ error: () => {} });
  }

  private mergeRows(catalog: RoleAccessCatalogEntry[], grants: RoleAccessGrant[]): NavAccessRow[] {
    const g = new Map(grants.map((x) => [x.resourceCode, x.permitted]));
    return catalog.map((c) => ({
      resourceCode: c.code,
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
