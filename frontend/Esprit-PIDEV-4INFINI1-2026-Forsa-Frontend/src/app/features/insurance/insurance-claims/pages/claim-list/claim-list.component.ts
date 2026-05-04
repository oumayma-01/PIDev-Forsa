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
import { InsuranceClaimService } from '../../../shared/services/insurance-claim.service';
import { InsuranceClaim } from '../../../shared/models/insurance.models';
import { ClaimStatus } from '../../../shared/enums/insurance.enums';
import type { ForsaIconName } from '../../../../../shared/ui/forsa-icon/forsa-icon.types';

@Component({
  selector: 'app-claim-list',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    DecimalPipe,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaDataTableComponent,
    ForsaIconComponent,
  ],
  templateUrl: './claim-list.component.html',
  styleUrl: './claim-list.component.css',
})
export class ClaimListComponent implements OnInit {
  private readonly svc = inject(InsuranceClaimService);

  claims = signal<InsuranceClaim[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  deletingId = signal<number | null>(null);

  readonly claimTablePageIndex = signal(0);
  readonly claimTablePageSize = signal(10);
  claimTablePageSizeOptions: number[] = [5, 10, 25, 50];

  readonly claimTableColumns: ForsaTableColumn[] = [
    { key: 'claimNumber', label: 'Claim #', width: '9rem' },
    { key: 'policy', label: 'Policy', width: '9rem' },
    { key: 'type', label: 'Type' },
    { key: 'status', label: 'Status', width: '9rem' },
    { key: 'claimAmount', label: 'Claim amount', align: 'right', width: '8rem' },
    { key: 'approved', label: 'Approved', align: 'right', width: '8rem' },
    { key: 'indemnif', label: 'Indemnif. paid', align: 'right', width: '8rem' },
    { key: 'incident', label: 'Incident date', width: '8rem' },
    { key: 'actions', label: 'Actions', align: 'right', width: '9rem' },
  ];

  readonly pagedClaims = computed(() => {
    const list = this.claims();
    const start = this.claimTablePageIndex() * this.claimTablePageSize();
    return list.slice(start, start + this.claimTablePageSize());
  });

  constructor() {
    effect(
      () => {
        const total = this.claims().length;
        const sz = this.claimTablePageSize();
        const maxIdx = Math.max(0, Math.ceil(total / sz) - 1);
        untracked(() => {
          if (this.claimTablePageIndex() > maxIdx) {
            this.claimTablePageIndex.set(maxIdx);
          }
        });
      },
      { allowSignalWrites: true },
    );
  }

  ngOnInit(): void {
    this.load();
  }

  onClaimTablePage(ev: ForsaDataTablePageEvent): void {
    this.claimTablePageIndex.set(ev.pageIndex);
    this.claimTablePageSize.set(ev.pageSize);
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: (data) => {
        this.claims.set(data);
        this.claimTablePageIndex.set(0);
        this.loading.set(false);
      },
      error: (e) => { this.error.set(e.message ?? 'Failed to load claims.'); this.loading.set(false); },
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this claim?')) return;
    this.deletingId.set(id);
    this.svc.delete(id).subscribe({
      next: () => { this.claims.update((c) => c.filter((x) => x.id !== id)); this.deletingId.set(null); },
      error: (e) => { alert(e.message ?? 'Delete failed.'); this.deletingId.set(null); },
    });
  }

  statusTone(s?: ClaimStatus): 'success' | 'warning' | 'danger' | 'info' | 'muted' {
    switch (s) {
      case ClaimStatus.PAID: return 'success';
      case ClaimStatus.APPROVED: return 'info';
      case ClaimStatus.PENDING: return 'warning';
      case ClaimStatus.REJECTED: return 'danger';
      case ClaimStatus.CLOSED: return 'muted';
      default: return 'muted';
    }
  }

  statusIcon(s?: ClaimStatus): ForsaIconName {
    switch (s) {
      case ClaimStatus.PAID: return 'check-circle-2';
      case ClaimStatus.APPROVED: return 'check-circle-2';
      case ClaimStatus.PENDING: return 'clock';
      case ClaimStatus.REJECTED: return 'alert-circle';
      case ClaimStatus.CLOSED: return 'history';
      default: return 'history';
    }
  }
}
