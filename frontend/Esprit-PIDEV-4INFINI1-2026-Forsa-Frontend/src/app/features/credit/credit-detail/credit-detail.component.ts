import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { filter, map, switchMap, tap } from 'rxjs/operators';
import type {
  AmortizationScheduleResponse,
  CreditRequestApi,
  RepaymentScheduleApi,
} from '../../../core/models/credit-api.model';
import { AuthService } from '../../../core/services/auth.service';
import { CreditApiService } from '../../../core/services/credit-api.service';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';

@Component({
  selector: 'app-credit-detail',
  standalone: true,
  imports: [
    FormsModule,
    DecimalPipe,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaInputDirective,
  ],
  templateUrl: './credit-detail.component.html',
  styleUrl: './credit-detail.component.css',
})
export class CreditDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly api = inject(CreditApiService);
  private readonly auth = inject(AuthService);

  readonly credit = signal<CreditRequestApi | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);

  readonly schedule = signal<AmortizationScheduleResponse | null>(null);
  readonly scheduleLoading = signal(false);
  readonly scheduleError = signal<string | null>(null);

  readonly repayments = signal<RepaymentScheduleApi[]>([]);
  readonly repaymentsLoading = signal(false);
  readonly repaymentsError = signal<string | null>(null);

  readonly actionMessage = signal<string | null>(null);
  readonly actionError = signal<string | null>(null);
  readonly actionBusy = signal(false);
  readonly payBusyId = signal<number | null>(null);

  rejectReason = '';

  readonly roles = computed(() => this.auth.currentUser()?.roles ?? []);
  readonly isAdmin = computed(() => this.roles().includes('ROLE_ADMIN'));
  readonly isAgent = computed(() => this.roles().includes('ROLE_AGENT'));
  readonly isClient = computed(() => this.roles().includes('ROLE_CLIENT'));

  readonly canSeeRiskReport = computed(() => this.isAdmin() || this.isAgent());

  constructor() {
    this.route.paramMap
      .pipe(
        map((m) => Number(m.get('id'))),
        tap(() => {
          this.error.set(null);
          this.actionMessage.set(null);
          this.actionError.set(null);
          this.credit.set(null);
          this.schedule.set(null);
          this.scheduleError.set(null);
          this.repayments.set([]);
          this.repaymentsError.set(null);
        }),
        filter((id) => Number.isFinite(id) && id > 0),
        tap(() => this.loading.set(true)),
        switchMap((id) => this.api.getCreditById(id)),
        takeUntilDestroyed(),
      )
      .subscribe({
        next: (c) => {
          this.loading.set(false);
          this.credit.set(c);
          this.loadScheduleIfAllowed(c);
          if (this.isRepaymentScheduleAvailable(c.status)) {
            this.loadRepayments(c.id);
          } else {
            this.repaymentsLoading.set(false);
            this.repaymentsError.set(null);
            this.repayments.set([]);
          }
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(this.readError(err));
        },
      });
  }

  refresh(): void {
    const id = this.credit()?.id;
    if (!id) return;

    this.error.set(null);
    this.actionMessage.set(null);
    this.actionError.set(null);

    this.loading.set(true);
    this.api.getCreditById(id).subscribe({
      next: (c) => {
        this.loading.set(false);
        this.credit.set(c);
        this.loadScheduleIfAllowed(c);
        if (this.isRepaymentScheduleAvailable(c.status)) {
          this.loadRepayments(c.id);
        } else {
          this.repaymentsLoading.set(false);
          this.repaymentsError.set(null);
          this.repayments.set([]);
        }
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(this.readError(err));
      },
    });
  }

  approve(): void {
    const c = this.credit();
    if (!c) return;

    this.actionError.set(null);
    this.actionMessage.set(null);

    this.actionBusy.set(true);
    this.api.approveCredit(c.id).subscribe({
      next: (updated) => {
        this.actionBusy.set(false);
        this.actionMessage.set('Credit approved.');
        this.credit.set(updated);
        this.loadScheduleIfAllowed(updated);
        if (this.isRepaymentScheduleAvailable(updated.status)) {
          this.loadRepayments(updated.id);
        } else {
          this.repaymentsLoading.set(false);
          this.repaymentsError.set(null);
          this.repayments.set([]);
        }
      },
      error: (err) => {
        this.actionBusy.set(false);
        this.actionError.set(this.readError(err));
      },
    });
  }

  reject(): void {
    const c = this.credit();
    if (!c) return;

    this.actionError.set(null);
    this.actionMessage.set(null);

    const reason = this.rejectReason.trim();
    if (!reason) {
      this.actionError.set('Please enter a rejection reason.');
      return;
    }

    this.actionBusy.set(true);
    this.api.rejectCredit(c.id, { reason }).subscribe({
      next: (updated) => {
        this.actionBusy.set(false);
        this.actionMessage.set('Credit rejected.');
        this.credit.set(updated);
      },
      error: (err) => {
        this.actionBusy.set(false);
        this.actionError.set(this.readError(err));
      },
    });
  }

  pay(r: RepaymentScheduleApi): void {
    if (r.status === 'PAID') return;

    this.actionError.set(null);
    this.actionMessage.set(null);

    this.payBusyId.set(r.id);
    this.api.payRepayment(r.id).subscribe({
      next: (updated) => {
        this.payBusyId.set(null);
        this.actionMessage.set('Payment recorded.');
        this.repayments.update((list) => list.map((x) => (x.id === updated.id ? updated : x)));
        // refresh credit status (ACTIVE/REPAID)
        const creditId = this.credit()?.id;
        if (creditId) {
          this.api.getCreditById(creditId).subscribe({
            next: (c) => this.credit.set(c),
            error: () => {
              /* ignore */
            },
          });
        }
      },
      error: (err) => {
        this.payBusyId.set(null);
        this.actionError.set(this.readError(err));
      },
    });
  }

  statusTone(status: CreditRequestApi['status']): 'success' | 'warning' | 'danger' | 'info' {
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

  repaymentTone(status: RepaymentScheduleApi['status']): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case 'PAID':
        return 'success';
      case 'LATE':
        return 'danger';
      case 'PENDING':
      default:
        return 'warning';
    }
  }

  private loadScheduleIfAllowed(c: CreditRequestApi): void {
    this.schedule.set(null);
    this.scheduleError.set(null);

    if (!c.status || c.status === 'SUBMITTED' || c.status === 'UNDER_REVIEW') {
      return;
    }

    this.scheduleLoading.set(true);
    this.api.getAmortizationSchedule(c.id).subscribe({
      next: (res) => {
        this.scheduleLoading.set(false);
        this.schedule.set(res);
      },
      error: (err) => {
        this.scheduleLoading.set(false);
        this.scheduleError.set(this.readError(err));
      },
    });
  }

  private isRepaymentScheduleAvailable(status: CreditRequestApi['status']): boolean {
    return !!status && status !== 'SUBMITTED' && status !== 'UNDER_REVIEW';
  }

  private loadRepayments(creditId: number): void {
    this.repaymentsLoading.set(true);
    this.repaymentsError.set(null);
    this.api.getRepaymentsForCredit(creditId).subscribe({
      next: (list) => {
        this.repaymentsLoading.set(false);
        this.repayments.set(list ?? []);
      },
      error: (err) => {
        this.repaymentsLoading.set(false);
        this.repaymentsError.set(this.readError(err));
      },
    });
  }

  private readError(err: unknown): string {
    const body = (err as { error?: { message?: string; error?: string } })?.error;
    if (typeof body?.message === 'string' && body.message.trim()) {
      return body.message;
    }
    if (typeof body?.error === 'string' && body.error.trim()) {
      return body.error;
    }
    return 'Something went wrong. Please try again.';
  }
}
