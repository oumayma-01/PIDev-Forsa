import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, of } from 'rxjs';
import { MOCK_PARTNERS } from '../../../core/data/mock-data';
import type { ClientCashback, Partner, PartnerStatus, PartnerType } from '../../../core/models/forsa.models';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { PARTNER_TYPE_LABELS, PartnerTypeLabelPipe } from './partenariat-type-label-public';
import { PartnerService } from '../services/partner.service';

@Component({
  selector: 'app-partenariat-list',
  standalone: true,
  templateUrl: './partenariat-list.component.html',
  styleUrl: './partenariat-list.component.css',
  imports: [
    DecimalPipe,
    FormsModule,
    RouterLink,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
    PartnerTypeLabelPipe,
  ],
})
export class PartenariatListComponent implements OnInit {
  private readonly partnerService = inject(PartnerService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);

  readonly partners = signal<Partner[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly cashback = signal<ClientCashback | null>(null);

  readonly searchQuery = signal('');
  readonly statusFilter = signal<PartnerStatus | 'ALL'>('ALL');
  readonly typeFilter = signal<PartnerType | 'ALL'>('ALL');

  readonly actionLoading = signal<number | null>(null);
  readonly showSuspendFormId = signal<number | null>(null);
  suspendReason = '';

  get isAdmin(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_ADMIN') ?? false;
  }

  get isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_CLIENT') || roles.includes('CLIENT');
  }

  readonly filteredPartners = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const st = this.statusFilter();
    const ty = this.typeFilter();
    return this.partners().filter((p) => {
      if (st !== 'ALL' && p.status !== st) return false;
      if (ty !== 'ALL' && p.partnerType !== ty) return false;
      if (!q) return true;
      return `${p.businessName} ${p.city} ${p.partnerType}`.toLowerCase().includes(q);
    });
  });

  readonly topRated = computed(() =>
    [...this.partners()]
      .filter((p) => p.status === 'ACTIVE' && (p.averageRating ?? 0) > 0)
      .sort((a, b) => (b.averageRating ?? 0) - (a.averageRating ?? 0))
      .slice(0, 5),
  );

  readonly adminStats = computed(() => {
    const list = this.partners();
    const active = list.filter((p) => p.status === 'ACTIVE').length;
    const totalVolume = list.reduce((s, p) => s + (p.totalAmountProcessed ?? 0), 0);
    const totalCommission = list.reduce(
      (s, p) => s + (p.totalAmountProcessed ?? 0) * (p.commissionRate / 100),
      0,
    );
    return { total: list.length, active, totalVolume, totalCommission };
  });

  readonly statusOptions: { value: PartnerStatus | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'All statuses' },
    { value: 'PENDING', label: 'Pending' },
    { value: 'ACTIVE', label: 'Active' },
    { value: 'SUSPENDED', label: 'Suspended' },
    { value: 'REJECTED', label: 'Rejected' },
    { value: 'CLOSED', label: 'Closed' },
  ];

  readonly typeOptions: { value: PartnerType | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'All types' },
    ...(Object.keys(PARTNER_TYPE_LABELS) as PartnerType[]).map((value) => ({
      value,
      label: PARTNER_TYPE_LABELS[value],
    })),
  ];

  ngOnInit(): void {
    const load$ = this.isAdmin
      ? this.partnerService.getAllPartners()
      : this.partnerService.getActivePartners();

    load$.pipe(catchError(() => of(MOCK_PARTNERS))).subscribe({
      next: (data) => {
        this.partners.set(data);
        this.loading.set(false);
      },
    });

    if (this.isClient) {
      const uid = this.auth.currentUser()?.id;
      if (uid) {
        this.partnerService.getClientCashback(Number(uid)).subscribe({
          next: (c) => this.cashback.set(c),
          error: () => {},
        });
      }
    }
  }

  onSearch(value: string): void {
    this.searchQuery.set(value);
  }

  onStatusSelect(ev: Event): void {
    this.statusFilter.set((ev.target as HTMLSelectElement).value as PartnerStatus | 'ALL');
  }

  onTypeSelect(ev: Event): void {
    this.typeFilter.set((ev.target as HTMLSelectElement).value as PartnerType | 'ALL');
  }

  openDetail(id: number): void {
    this.router.navigate(['/dashboard/partenariat', id]);
  }

  statusTone(status: PartnerStatus): 'success' | 'warning' | 'danger' | 'muted' {
    switch (status) {
      case 'ACTIVE':    return 'success';
      case 'PENDING':   return 'warning';
      case 'SUSPENDED': return 'danger';
      case 'REJECTED':  return 'danger';
      case 'CLOSED':    return 'muted';
    }
  }

  badgeTone(badge: Partner['badge']): 'default' | 'secondary' | 'warning' | 'info' | 'muted' {
    switch (badge) {
      case 'DIAMOND': return 'info';
      case 'GOLD':    return 'warning';
      case 'SILVER':  return 'secondary';
      case 'BRONZE':  return 'default';
      default:        return 'muted';
    }
  }

  // ── Admin actions ──────────────────────────────────────────────────────────

  activatePartner(p: Partner): void {
    this.actionLoading.set(p.id);
    this.partnerService.activatePartner(p.id).subscribe({
      next: (u) => { this.replacePartner(u); this.actionLoading.set(null); },
      error: () => this.actionLoading.set(null),
    });
  }

  openSuspendForm(id: number): void {
    this.showSuspendFormId.set(id);
    this.suspendReason = '';
  }

  cancelSuspend(): void {
    this.showSuspendFormId.set(null);
  }

  confirmSuspend(p: Partner): void {
    if (!this.suspendReason.trim()) return;
    this.actionLoading.set(p.id);
    this.partnerService.suspendPartner(p.id, this.suspendReason).subscribe({
      next: (u) => {
        this.replacePartner(u);
        this.actionLoading.set(null);
        this.showSuspendFormId.set(null);
      },
      error: () => this.actionLoading.set(null),
    });
  }

  reactivatePartner(p: Partner): void {
    this.actionLoading.set(p.id);
    this.partnerService.reactivatePartner(p.id).subscribe({
      next: (u) => { this.replacePartner(u); this.actionLoading.set(null); },
      error: () => this.actionLoading.set(null),
    });
  }

  private replacePartner(updated: Partner): void {
    this.partners.update((list) => list.map((x) => (x.id === updated.id ? updated : x)));
  }
}
