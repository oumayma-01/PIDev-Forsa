import {
  Component,
  DestroyRef,
  HostListener,
  OnInit,
  computed,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../../environments/environment';
import type {
  ManagedUser,
  ForsaRoleName,
} from '../../../core/models/user-admin.model';
import { UserAdminService } from '../../../core/services/user-admin.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaDataTableComponent } from '../../../shared/ui/forsa-data-table/forsa-data-table.component';
import type {
  ForsaDataTablePageEvent,
  ForsaTableColumn,
} from '../../../shared/ui/forsa-data-table/forsa-data-table.types';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaPasswordFieldComponent } from '../../../shared/ui/forsa-password-field/forsa-password-field.component';

type DialogMode = 'closed' | 'edit' | 'add-agent';

type StatusFilter = 'all' | 'active' | 'inactive';

/** Row actions menu rendered fixed (escapes table overflow clipping). */
type RowMenuState = { user: ManagedUser; top: number; right: number };

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    FormsModule,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaDataTableComponent,
    ForsaIconComponent,
    ForsaInputDirective,
    ForsaPasswordFieldComponent,
  ],
  templateUrl: './user-management.component.html',
  styleUrl: './user-management.component.css',
})
export class UserManagementComponent implements OnInit {
  private readonly usersApi = inject(UserAdminService);
  private readonly destroyRef = inject(DestroyRef);

  readonly users = signal<ManagedUser[]>([]);
  readonly loading = signal(false);
  readonly busyId = signal<number | null>(null);
  readonly dialogSaving = signal(false);
  readonly deleteConfirmUser = signal<ManagedUser | null>(null);
  readonly deleteSubmitting = signal(false);
  readonly banner = signal<{ tone: 'ok' | 'err'; text: string } | null>(null);

  readonly searchText = signal('');
  readonly statusFilter = signal<StatusFilter>('all');
  readonly roleFilter = signal<'all' | ForsaRoleName>('all');

  readonly pageSize = signal(10);
  /** Zero-based page index (same contract as {@link ForsaDataTableComponent}) */
  readonly pageIndex = signal(0);

  readonly userTableColumns: ForsaTableColumn[] = [
    { key: 'id', label: 'ID' },
    { key: 'username', label: 'Username' },
    { key: 'email', label: 'Email' },
    { key: 'role', label: 'Role' },
    { key: 'status', label: 'Status' },
    { key: 'actions', label: 'Actions', align: 'right' },
  ];

  readonly filteredUsers = computed(() => {
    let list = this.users();
    const q = this.searchText().trim().toLowerCase();
    if (q) {
      list = list.filter(
        (u) =>
          u.username.toLowerCase().includes(q) ||
          u.email.toLowerCase().includes(q) ||
          String(u.id).includes(q),
      );
    }
    const st = this.statusFilter();
    if (st === 'active') {
      list = list.filter((u) => u.isActive === true);
    } else if (st === 'inactive') {
      list = list.filter((u) => u.isActive !== true);
    }
    const rf = this.roleFilter();
    if (rf !== 'all') {
      list = list.filter((u) => u.role.name === rf);
    }
    return list;
  });

  readonly paginatedUsers = computed(() => {
    const list = this.filteredUsers();
    const sz = this.pageSize();
    if (!list.length) {
      return [];
    }
    const totalPages = Math.max(1, Math.ceil(list.length / sz));
    const idx = Math.min(this.pageIndex(), totalPages - 1);
    const start = idx * sz;
    return list.slice(start, start + sz);
  });

  readonly dialogMode = signal<DialogMode>('closed');
  readonly editUserId = signal<number | null>(null);
  readonly rowMenu = signal<RowMenuState | null>(null);
  formUsername = '';
  formEmail = '';
  formPassword = '';
  formRoleId: number = environment.defaultClientRoleId;

  readonly roleOptions: { id: number; label: string }[] = [
    { id: 1, label: 'Admin' },
    { id: 2, label: 'Client' },
    { id: 3, label: 'Agent' },
  ];

  readonly statusFilterOptions: { value: StatusFilter; label: string }[] = [
    { value: 'all', label: 'All statuses' },
    { value: 'active', label: 'Active' },
    { value: 'inactive', label: 'Inactive' },
  ];

  readonly roleFilterOptions: { value: 'all' | ForsaRoleName; label: string }[] = [
    { value: 'all', label: 'All roles' },
    { value: 'CLIENT', label: 'Client' },
    { value: 'AGENT', label: 'Agent' },
    { value: 'ADMIN', label: 'Admin' },
  ];

  /** Use `number[]` (not `readonly number[]`) so `[pageSizeOptions]` satisfies `ForsaDataTableComponent` under strict template checks (NG4). */
  pageSizeOptions: number[] = [5, 10, 25, 50];

  constructor() {
    effect(() => {
      this.searchText();
      this.statusFilter();
      this.roleFilter();
      untracked(() => this.pageIndex.set(0));
    });
    effect(
      () => {
        const list = this.filteredUsers();
        const sz = this.pageSize();
        const totalPages = Math.max(1, Math.ceil(list.length / sz) || 1);
        const maxIdx = totalPages - 1;
        untracked(() => {
          if (this.pageIndex() > maxIdx) {
            this.pageIndex.set(maxIdx);
          }
        });
      },
      { allowSignalWrites: true },
    );
  }

  ngOnInit(): void {
    this.refresh();
    const closeMenuOnScrollOrResize = (): void => {
      if (this.rowMenu()) {
        this.closeRowMenu();
      }
    };
    document.addEventListener('scroll', closeMenuOnScrollOrResize, true);
    window.addEventListener('resize', closeMenuOnScrollOrResize);
    this.destroyRef.onDestroy(() => {
      document.removeEventListener('scroll', closeMenuOnScrollOrResize, true);
      window.removeEventListener('resize', closeMenuOnScrollOrResize);
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(ev: MouseEvent): void {
    const t = ev.target;
    const el = t instanceof Element ? t : (t instanceof Node ? t.parentElement : null);
    if (el?.closest('.um-row-menu')) {
      return;
    }
    if (el?.closest('.um-actions-cell')) {
      return;
    }
    this.closeRowMenu();
  }

  refresh(): void {
    this.loading.set(true);
    this.banner.set(null);
    this.usersApi.listUsers().subscribe({
      next: (rows) => {
        this.users.set(rows);
        this.loading.set(false);
      },
      error: (e) => {
        this.loading.set(false);
        this.banner.set({ tone: 'err', text: e.error?.message ?? 'Could not load users.' });
      },
    });
  }

  clearFilters(): void {
    this.searchText.set('');
    this.statusFilter.set('all');
    this.roleFilter.set('all');
  }

  onDataTablePage(ev: ForsaDataTablePageEvent): void {
    this.pageIndex.set(ev.pageIndex);
    this.pageSize.set(ev.pageSize);
  }

  toggleRowMenu(u: ManagedUser, ev: MouseEvent): void {
    const cur = this.rowMenu();
    if (cur?.user.id === u.id) {
      this.rowMenu.set(null);
      return;
    }
    const anchor = ev.currentTarget;
    if (!(anchor instanceof HTMLElement)) {
      return;
    }
    const r = anchor.getBoundingClientRect();
    this.rowMenu.set({
      user: u,
      top: r.bottom-60,
      right: window.innerWidth - r.right-10,
    });
  }

  closeRowMenu(): void {
    this.rowMenu.set(null);
  }

  roleTone(role: ForsaRoleName): 'success' | 'warning' | 'danger' | 'info' {
    switch (role) {
      case 'ADMIN':
        return 'danger';
      case 'AGENT':
        return 'info';
      default:
        return 'success';
    }
  }

  activeLabel(u: ManagedUser): string {
    return u.isActive ? 'Active' : 'Inactive';
  }

  openAddAgent(): void {
    this.closeRowMenu();
    this.dialogMode.set('add-agent');
    this.editUserId.set(null);
    this.formUsername = '';
    this.formEmail = '';
    this.formPassword = '';
    this.formRoleId = environment.defaultAgentRoleId;
    this.banner.set(null);
  }

  openEdit(u: ManagedUser): void {
    this.closeRowMenu();
    this.dialogMode.set('edit');
    this.editUserId.set(u.id);
    this.formUsername = u.username;
    this.formEmail = u.email;
    this.formPassword = '';
    this.formRoleId = u.role.id;
    this.banner.set(null);
  }

  closeDialog(): void {
    this.dialogMode.set('closed');
    this.editUserId.set(null);
  }

  submitDialog(): void {
    const mode = this.dialogMode();
    if (mode === 'closed') {
      return;
    }
    if (!this.formUsername.trim() || !this.formEmail.trim() || this.formPassword.length < 6) {
      this.banner.set({ tone: 'err', text: 'Username, email and password (6+ characters) are required.' });
      return;
    }
    const payload = {
      username: this.formUsername.trim(),
      email: this.formEmail.trim(),
      password: this.formPassword,
      idrole: this.formRoleId,
    };
    if (mode === 'add-agent') {
      const agentPayload = { ...payload, idrole: environment.defaultAgentRoleId };
      this.dialogSaving.set(true);
      this.usersApi.createAgent(agentPayload).subscribe({
        next: (res) => {
          this.banner.set({ tone: 'ok', text: res.message });
          this.closeDialog();
          this.refresh();
          this.dialogSaving.set(false);
        },
        error: (e) => {
          this.banner.set({ tone: 'err', text: e.error?.message ?? 'Could not create agent.' });
          this.dialogSaving.set(false);
        },
      });
      return;
    }
    const id = this.editUserId();
    if (id == null) {
      return;
    }
    this.dialogSaving.set(true);
    this.usersApi.updateUser(id, payload).subscribe({
      next: (res) => {
        this.banner.set({ tone: 'ok', text: res.message });
        this.closeDialog();
        this.refresh();
        this.dialogSaving.set(false);
      },
      error: (e) => {
        this.banner.set({ tone: 'err', text: e.error?.message ?? 'Update failed.' });
        this.dialogSaving.set(false);
      },
    });
  }

  toggleActive(u: ManagedUser): void {
    this.closeRowMenu();
    const next = !u.isActive;
    this.busyId.set(u.id);
    this.usersApi.setUserActive(u.id, next).subscribe({
      next: (res) => {
        this.banner.set({ tone: 'ok', text: res.message });
        this.refresh();
        this.busyId.set(null);
      },
      error: (e) => {
        this.banner.set({ tone: 'err', text: e.error?.message ?? 'Could not update status.' });
        this.busyId.set(null);
      },
    });
  }

  openDeleteConfirm(u: ManagedUser): void {
    this.closeRowMenu();
    this.deleteConfirmUser.set(u);
  }

  closeDeleteConfirm(): void {
    if (this.deleteSubmitting()) {
      return;
    }
    this.deleteConfirmUser.set(null);
  }

  confirmDelete(): void {
    const u = this.deleteConfirmUser();
    if (!u) {
      return;
    }
    this.deleteSubmitting.set(true);
    this.busyId.set(u.id);
    this.usersApi.deleteUser(u.id).subscribe({
      next: () => {
        this.banner.set({ tone: 'ok', text: 'User deleted.' });
        this.deleteConfirmUser.set(null);
        this.refresh();
        this.busyId.set(null);
        this.deleteSubmitting.set(false);
      },
      error: (e) => {
        this.banner.set({ tone: 'err', text: e.error?.message ?? 'Delete failed.' });
        this.busyId.set(null);
        this.deleteSubmitting.set(false);
      },
    });
  }

  rowBusy(u: ManagedUser): boolean {
    return this.busyId() === u.id;
  }

  filtersActive(): boolean {
    return (
      this.searchText().trim() !== '' ||
      this.statusFilter() !== 'all' ||
      this.roleFilter() !== 'all'
    );
  }
}
