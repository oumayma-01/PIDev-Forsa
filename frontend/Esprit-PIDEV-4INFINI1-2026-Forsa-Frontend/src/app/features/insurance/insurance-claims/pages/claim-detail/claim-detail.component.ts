import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsuranceClaimService } from '../../../shared/services/insurance-claim.service';
import { InsuranceClaim } from '../../../shared/models/insurance.models';
import { ClaimStatus } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-claim-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './claim-detail.component.html',
  styleUrl: './claim-detail.component.css',
})
export class ClaimDetailComponent implements OnInit {
  private readonly svc = inject(InsuranceClaimService);
  private readonly route = inject(ActivatedRoute);

  claim = signal<InsuranceClaim | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.error.set('No ID provided'); this.loading.set(false); return; }
    this.svc.getById(+id).subscribe({
      next: (c) => { this.claim.set(c); this.loading.set(false); },
      error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
    });
  }

  statusTone(s?: ClaimStatus): 'success' | 'warning' | 'danger' | 'info' | 'muted' {
    switch (s) {
      case ClaimStatus.PAID: return 'success';
      case ClaimStatus.APPROVED: return 'info';
      case ClaimStatus.PENDING: return 'warning';
      case ClaimStatus.REJECTED: return 'danger';
      default: return 'muted';
    }
  }
}
