import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsuranceClaimService } from '../../../shared/services/insurance-claim.service';
import { InsuranceClaim } from '../../../shared/models/insurance.models';
import { ClaimStatus } from '../../../shared/enums/insurance.enums';
import type { ForsaIconName } from '../../../../../shared/ui/forsa-icon/forsa-icon.types';

@Component({
  selector: 'app-claim-list',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './claim-list.component.html',
  styleUrl: './claim-list.component.css',
})
export class ClaimListComponent implements OnInit {
  private readonly svc = inject(InsuranceClaimService);

  claims = signal<InsuranceClaim[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  deletingId = signal<number | null>(null);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: (data) => { this.claims.set(data); this.loading.set(false); },
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
