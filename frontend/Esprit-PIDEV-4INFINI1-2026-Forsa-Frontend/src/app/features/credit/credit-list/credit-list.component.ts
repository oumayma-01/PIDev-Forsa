import { DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Subscription } from 'rxjs';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import type { CreditRequestApi, CreditStatus, RepaymentScheduleApi, RepaymentStatus } from '../../../core/models/credit-api.model';
import { AuthService } from '../../../core/services/auth.service';
import { CreditApiService } from '../../../core/services/credit-api.service';

@Component({
  selector: 'app-credit-list',
  standalone: true,
  imports: [
    DecimalPipe,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
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

  readonly activeCredit = computed<CreditRequestApi | null>(() => {
    const list = this.credits() ?? [];
    return list.find((c) => c.status === 'ACTIVE' || c.status === 'APPROVED') ?? null;
  });

  constructor() {
    void this.load();
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
      this.error.set('Session expirée. Veuillez vous reconnecter.');
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
          this.credits.set(list ?? []);

          this.api.consumeMyGiftAwardNotification().subscribe({
            next: (res) => {
              if (res?.show) {
                this.showGiftAlert(res.amount);
              }
            },
            error: () => {
              // Non-blocking: we do not prevent the page from loading if the gift notification endpoint fails.
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
      this.error.set("Accès indisponible: rôle requis AGENT ou ADMIN.");
      return;
    }

    this.loading.set(true);
    const req$ = this.isAdmin() ? this.api.listAllCredits() : this.api.listPendingCredits();
    this.inflight = req$.subscribe({
      next: (list) => {
        this.loading.set(false);
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
      return 'Impossible de joindre le serveur. Vérifiez que le backend est lancé et que CORS est autorisé.';
    }
    if (status === 401) {
      return 'Non authentifié. Veuillez vous reconnecter.';
    }
    if (status === 403) {
      return 'Accès refusé. Cette page requiert le rôle AGENT (ou ADMIN pour la vue globale).';
    }
    if (status === 500) {
      const base = 'Erreur serveur (500) lors du chargement des crédits.';
      const extra = backendMessage?.trim() ? ` Détail: ${backendMessage.trim()}` : '';
      const where = url ? ` Endpoint: ${url}` : '';
      return `${base}${where}${extra}`;
    }

    if (backendMessage?.trim()) {
      return backendMessage.trim();
    }
    if (typeof anyErr?.message === 'string' && anyErr.message.trim()) {
      return anyErr.message.trim();
    }
    return 'Erreur lors du chargement des crédits.';
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
