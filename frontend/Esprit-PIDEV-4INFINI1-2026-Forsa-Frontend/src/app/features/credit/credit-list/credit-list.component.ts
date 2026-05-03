import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { Subscription } from 'rxjs';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaDataTableComponent } from '../../../shared/ui/forsa-data-table/forsa-data-table.component';
import type {
  ForsaDataTablePageEvent,
  ForsaTableColumn,
} from '../../../shared/ui/forsa-data-table/forsa-data-table.types';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import type { CreditRequestApi, CreditStatus, RepaymentScheduleApi, RepaymentStatus } from '../../../core/models/credit-api.model';
import { AuthService } from '../../../core/services/auth.service';
import { CreditApiService } from '../../../core/services/credit-api.service';

@Component({
  selector: 'app-credit-list',
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaDataTableComponent,
    ForsaIconComponent,
  ],
  templateUrl: './credit-list.component.html',
  styleUrl: './credit-list.component.css',
})
export class CreditListComponent {
  private readonly api = inject(CreditApiService);
  private readonly auth = inject(AuthService);

  private inflight?: Subscription;

  readonly credits = signal<CreditRequestApi[]>([]);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly repayments = signal<RepaymentScheduleApi[]>([]);
  readonly repaymentsLoading = signal(false);
  readonly repaymentsError = signal<string | null>(null);

  readonly giftAlertVisible = signal(false);
  readonly giftAmount = signal<number | null>(null);
  private giftAlertTimeoutId: ReturnType<typeof setTimeout> | null = null;

  readonly roles = computed(() => this.auth.currentUser()?.roles ?? []);
  readonly isAdmin = computed(() => this.roles().includes('ROLE_ADMIN'));
  readonly isAgent = computed(() => this.roles().includes('ROLE_AGENT'));
  readonly isClient = computed(() => this.roles().includes('ROLE_CLIENT'));

  readonly searchTerm = signal('');
  readonly statusFilter = signal<string>('');
  readonly dateStart = signal<string>('');
  readonly dateEnd = signal<string>('');

  readonly creditStaffColumns: ForsaTableColumn[] = [
    { key: 'id', label: 'ID', width: '4.5rem' },
    { key: 'client', label: 'Client' },
    { key: 'amount', label: 'Amount', align: 'right', width: '8rem' },
    { key: 'duration', label: 'Duration', width: '6rem' },
    { key: 'status', label: 'Status', width: '9rem' },
    { key: 'date', label: 'Date', width: '9rem' },
    { key: 'agent', label: 'Agent', width: '5rem' },
    { key: 'actions', label: 'Actions', align: 'right', width: '8.5rem' },
  ];

  readonly repaymentColumns: ForsaTableColumn[] = [
    { key: 'id', label: 'ID', width: '4rem' },
    { key: 'due', label: 'Due date' },
    { key: 'amount', label: 'Amount', align: 'right' },
    { key: 'status', label: 'Status', width: '8rem' },
  ];

  readonly creditPageIndex = signal(0);
  readonly creditPageSize = signal(10);
  readonly creditPageSizeOptions: ReadonlyArray<number> = [5, 10, 25, 50];

  readonly repaymentPageIndex = signal(0);
  readonly repaymentPageSize = signal(10);
  readonly repaymentPageSizeOptions: ReadonlyArray<number> = [5, 10, 25, 50];

  readonly filteredCredits = computed(() => {
    let list = this.credits() ?? [];
    const term = this.searchTerm().toLowerCase().trim();
    const status = this.statusFilter();
    const start = this.dateStart();
    const end = this.dateEnd();

    // Global search term
    if (term) {
      list = list.filter((c) => {
        const idStr = c.id?.toString() || '';
        const userStr = (c.user?.username || '').toLowerCase();
        const statusStr = (c.status || '').toLowerCase();
        return idStr.includes(term) || userStr.includes(term) || statusStr.includes(term);
      });
    }

    // Status filter
    if (status) {
      list = list.filter((c) => c.status === status);
    }

    // Date interval filter
    if (start || end) {
      list = list.filter((c) => {
        if (!c.requestDate) return false;
        const d = new Date(c.requestDate);
        if (start && d < new Date(start)) return false;
        if (end) {
          const endDate = new Date(end);
          endDate.setHours(23, 59, 59, 999);
          if (d > endDate) return false;
        }
        return true;
      });
    }

    return list;
  });

  readonly creditsPaged = computed(() => {
    const list = this.filteredCredits();
    const start = this.creditPageIndex() * this.creditPageSize();
    return list.slice(start, start + this.creditPageSize());
  });

  readonly repaymentsPaged = computed(() => {
    const list = this.repayments();
    const start = this.repaymentPageIndex() * this.repaymentPageSize();
    return list.slice(start, start + this.repaymentPageSize());
  });

  readonly activeCredit = computed<CreditRequestApi | null>(() => {
    const list = this.credits() ?? [];
    return list.find((c) => c.status === 'ACTIVE' || c.status === 'APPROVED') ?? null;
  });

  constructor() {
    effect(() => {
      this.searchTerm();
      this.statusFilter();
      this.dateStart();
      this.dateEnd();
      untracked(() => this.creditPageIndex.set(0));
    });
    effect(
      () => {
        const total = this.filteredCredits().length;
        const sz = this.creditPageSize();
        const maxIdx = Math.max(0, Math.ceil(total / sz) - 1);
        untracked(() => {
          if (this.creditPageIndex() > maxIdx) {
            this.creditPageIndex.set(maxIdx);
          }
        });
      },
      { allowSignalWrites: true },
    );
    effect(
      () => {
        const total = this.repayments().length;
        const sz = this.repaymentPageSize();
        const maxIdx = Math.max(0, Math.ceil(total / sz) - 1);
        untracked(() => {
          if (this.repaymentPageIndex() > maxIdx) {
            this.repaymentPageIndex.set(maxIdx);
          }
        });
      },
      { allowSignalWrites: true },
    );
    void this.load();
  }

  onCreditTablePage(ev: ForsaDataTablePageEvent): void {
    this.creditPageIndex.set(ev.pageIndex);
    this.creditPageSize.set(ev.pageSize);
  }

  onRepaymentTablePage(ev: ForsaDataTablePageEvent): void {
    this.repaymentPageIndex.set(ev.pageIndex);
    this.repaymentPageSize.set(ev.pageSize);
  }

  async load(): Promise<void> {
    if (this.inflight) {
      this.inflight.unsubscribe();
      this.inflight = undefined;
    }

    this.error.set(null);

    const ok = this.auth.currentUser() ? true : await this.auth.ensureSessionFromApi();
    if (!ok) {
      this.loading.set(false);
      this.error.set('Session expired. Please log in again.');
      return;
    }

    if (this.isClient()) {
      this.loading.set(true);
      this.repaymentsLoading.set(false);
      this.repaymentsError.set(null);
      this.repayments.set([]);

      this.inflight = this.api.listMyCredits().subscribe({
        next: (list) => {
          this.loading.set(false);
          this.creditPageIndex.set(0);
          this.credits.set(list ?? []);

          this.api.consumeMyGiftAwardNotification().subscribe({
            next: (res) => {
              if (res?.show) {
                this.showGiftAlert(res.amount);
              }
            },
            error: () => {
              // Non-blocking
            },
          });

          const active = (list ?? []).find((c) => c.status === 'ACTIVE' || c.status === 'APPROVED');
          if (active && this.isRepaymentScheduleAvailable(active.status)) {
            this.loadRepayments(active.id);
          }
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(this.readError(err));
        },
      });
      return;
    }

    if (!this.isAdmin() && !this.isAgent()) {
      this.credits.set([]);
      this.loading.set(false);
      this.error.set("Access denied: AGENT or ADMIN role required.");
      return;
    }

    this.loading.set(true);
    // Both Admin and Agent can now see all credits
    this.inflight = this.api.listAllCredits().subscribe({
      next: (list) => {
        this.loading.set(false);
        this.creditPageIndex.set(0);
        this.credits.set(list ?? []);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.readError(err));
      },
    });
  }

  statusTone(status?: CreditStatus | null): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case 'APPROVED':
      case 'REPAID':
        return 'success';
      case 'SUBMITTED':
        return 'warning';
      case 'DEFAULTED':
        return 'danger';
      case 'UNDER_REVIEW':
      case 'ACTIVE':
      default:
        return 'info';
    }
  }

  repaymentTone(status?: RepaymentStatus | null): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case 'PAID':
        return 'success';
      case 'LATE':
        return 'danger';
      case 'PENDING':
      default:
        return 'info';
    }
  }

  private isRepaymentScheduleAvailable(status?: CreditStatus | null): boolean {
    return !!status && status !== 'SUBMITTED' && status !== 'UNDER_REVIEW';
  }

  private loadRepayments(creditId: number): void {
    this.repaymentsLoading.set(true);
    this.repaymentsError.set(null);
    this.repayments.set([]);

    this.api.getRepaymentsForCredit(creditId).subscribe({
      next: (list) => {
        this.repaymentsLoading.set(false);
        this.repaymentPageIndex.set(0);
        this.repayments.set(list ?? []);
      },
      error: (err) => {
        this.repaymentsLoading.set(false);
        this.repaymentsError.set(this.readError(err));
      },
    });
  }

  private readError(err: unknown): string {
    const anyErr = err as {
      status?: number;
      url?: string;
      message?: string;
      error?: unknown;
    };

    const status = typeof anyErr?.status === 'number' ? anyErr.status : null;
    const url = typeof anyErr?.url === 'string' ? anyErr.url : null;
    const body = anyErr?.error as { message?: string; error?: string } | string | null | undefined;

    const backendMessage =
      typeof body === 'string'
        ? body
        : typeof body?.message === 'string'
          ? body.message
          : typeof body?.error === 'string'
            ? body.error
            : null;

    if (status === 0) {
      return 'Unable to reach the server. Please check your connection.';
    }
    if (status === 401) {
      return 'Not authenticated. Please log in again.';
    }
    if (status === 403) {
      return 'Access denied. You do not have the required permissions.';
    }
    if (status === 500) {
      const base = 'Server error (500) while loading credits.';
      const extra = backendMessage?.trim() ? ` Detail: ${backendMessage.trim()}` : '';
      return `${base}${extra}`;
    }

    if (backendMessage?.trim()) {
      return backendMessage.trim();
    }
    return 'An error occurred while loading credits.';
  }

  dismissGiftAlert(): void {
    this.giftAlertVisible.set(false);
    if (this.giftAlertTimeoutId) {
      clearTimeout(this.giftAlertTimeoutId);
      this.giftAlertTimeoutId = null;
    }
  }

  private showGiftAlert(amount?: number): void {
    if (amount != null) {
      this.giftAmount.set(amount);
    }
    this.giftAlertVisible.set(true);

    if (this.giftAlertTimeoutId) {
      clearTimeout(this.giftAlertTimeoutId);
    }

    this.giftAlertTimeoutId = setTimeout(() => {
      this.giftAlertVisible.set(false);
      this.giftAlertTimeoutId = null;
    }, 6500);
  }
}
