import { Component, computed, effect, inject, OnInit, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaDataTableComponent } from '../../../../../shared/ui/forsa-data-table/forsa-data-table.component';
import type {
  ForsaDataTablePageEvent,
  ForsaTableColumn,
} from '../../../../../shared/ui/forsa-data-table/forsa-data-table.types';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { InsurancePolicy } from '../../../shared/models/insurance.models';
import { PolicyStatus } from '../../../shared/enums/insurance.enums';
import type { ForsaIconName } from '../../../../../shared/ui/forsa-icon/forsa-icon.types';

import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-policy-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    DecimalPipe,
    FormsModule,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaDataTableComponent,
    ForsaIconComponent,
  ],
  templateUrl: './policy-list.component.html',
  styleUrl: './policy-list.component.css',
})
export class PolicyListComponent implements OnInit {
  private readonly svc = inject(InsurancePolicyService);

  policies = signal<InsurancePolicy[]>([]);
  searchTerm = signal('');
  statusFilter = signal<string>('ALL');
  loading = signal(true);
  error = signal<string | null>(null);
  deletingId = signal<number | null>(null);
  reviewingId = signal<number | null>(null);

  filteredPolicies = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const status = this.statusFilter();

    return this.policies().filter((p) => {
      const matchesSearch =
        !term ||
        p.policyNumber?.toLowerCase().includes(term) ||
        p.user?.username?.toLowerCase().includes(term);

      const matchesStatus = status === 'ALL' || p.status === status;

      return matchesSearch && matchesStatus;
    });
  });

  readonly policyTablePageIndex = signal(0);
  readonly policyTablePageSize = signal(10);
  /** Mutable array for strict template binding to {@link ForsaDataTableComponent}. */
  policyTablePageSizeOptions: number[] = [5, 10, 25, 50];

  readonly policyTableColumns: ForsaTableColumn[] = [
    { key: 'policyNumber', label: 'Policy #', width: '9rem' },
    { key: 'product', label: 'Product' },
    { key: 'status', label: 'Status', width: '10rem' },
    { key: 'premium', label: 'Premium', align: 'right', width: '8rem' },
    { key: 'coverage', label: 'Coverage', align: 'right', width: '8rem' },
    { key: 'start', label: 'Start', width: '7rem' },
    { key: 'end', label: 'End', width: '7rem' },
    { key: 'risk', label: 'Risk', width: '9rem' },
    { key: 'actions', label: 'Actions', align: 'right', width: '10rem' },
  ];

  readonly pagedPolicies = computed(() => {
    const list = this.filteredPolicies();
    const start = this.policyTablePageIndex() * this.policyTablePageSize();
    return list.slice(start, start + this.policyTablePageSize());
  });

  PolicyStatus = PolicyStatus; // Expose to template

  constructor() {
    effect(() => {
      this.searchTerm();
      this.statusFilter();
      untracked(() => this.policyTablePageIndex.set(0));
    });
    effect(
      () => {
        const total = this.filteredPolicies().length;
        const sz = this.policyTablePageSize();
        const maxIdx = Math.max(0, Math.ceil(total / sz) - 1);
        untracked(() => {
          if (this.policyTablePageIndex() > maxIdx) {
            this.policyTablePageIndex.set(maxIdx);
          }
        });
      },
      { allowSignalWrites: true },
    );
  }

  ngOnInit(): void {
    this.load();
  }

  onPolicyTablePage(ev: ForsaDataTablePageEvent): void {
    this.policyTablePageIndex.set(ev.pageIndex);
    this.policyTablePageSize.set(ev.pageSize);
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: (data) => {
        this.policies.set(data);
        this.policyTablePageIndex.set(0);
        this.loading.set(false);
      },
      error: (e) => { this.error.set(e.message ?? 'Failed to load policies.'); this.loading.set(false); },
    });
  }

  approve(id: number): void {
    this.reviewingId.set(id);
    this.svc.agentReview(id, PolicyStatus.ACTIVE).subscribe({
      next: (updated) => {
        this.policies.update((list) => list.map((p) => (p.id === id ? updated : p)));
        this.reviewingId.set(null);
      },
      error: (e) => { alert(e.message ?? 'Review failed.'); this.reviewingId.set(null); },
    });
  }

  reject(id: number): void {
    this.reviewingId.set(id);
    this.svc.agentReview(id, PolicyStatus.CANCELLED).subscribe({
      next: (updated) => {
        this.policies.update((list) => list.map((p) => (p.id === id ? updated : p)));
        this.reviewingId.set(null);
      },
      error: (e) => { alert(e.message ?? 'Review failed.'); this.reviewingId.set(null); },
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this policy?')) return;
    this.deletingId.set(id);
    this.svc.delete(id).subscribe({
      next: () => { this.policies.update((p) => p.filter((x) => x.id !== id)); this.deletingId.set(null); },
      error: (e) => { alert(e.message ?? 'Delete failed.'); this.deletingId.set(null); },
    });
  }

  statusTone(status?: PolicyStatus): 'success' | 'warning' | 'danger' | 'muted' | 'info' {
    switch (status) {
      case PolicyStatus.ACTIVE: return 'success';
      case PolicyStatus.PENDING: return 'warning';
      case PolicyStatus.SUSPENDED: return 'info';
      case PolicyStatus.CANCELLED:
      case PolicyStatus.EXPIRED: return 'danger';
      default: return 'muted';
    }
  }

  statusIcon(status?: PolicyStatus): ForsaIconName {
    switch (status) {
      case PolicyStatus.ACTIVE: return 'check-circle-2';
      case PolicyStatus.PENDING: return 'clock';
      case PolicyStatus.SUSPENDED: return 'history';
      case PolicyStatus.CANCELLED: return 'alert-circle';
      case PolicyStatus.EXPIRED: return 'alert-circle';
      default: return 'history';
    }
  }
}
