import { Component, inject, OnInit, signal } from '@angular/core';
import { timeout } from 'rxjs';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
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
  imports: [CommonModule, RouterLink, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './claim-detail.component.html',
  styleUrl: './claim-detail.component.css',
})
export class ClaimDetailComponent implements OnInit {
  public readonly svc = inject(InsuranceClaimService);
  private readonly route = inject(ActivatedRoute);

  claim = signal<InsuranceClaim | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('No ID provided');
      this.loading.set(false);
      return;
    }

    // Add a 10s timeout to prevent endless loading in case of network/backend hang
    this.svc.getById(+id)
      .pipe(
        timeout(10000)
      )
      .subscribe({
        next: (c) => {
          if (!c) {
            this.error.set('Claim not found.');
          } else {
            this.claim.set(c);
          }
          this.loading.set(false);
        },
        error: (e) => {
          console.error('Error loading claim:', e);
          this.error.set(e.name === 'TimeoutError' ? 'Request timed out. Please try again.' : (e.message ?? 'Failed to load claim details.'));
          this.loading.set(false);
        },
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

  get parsedDynamicData(): { key: string, value: any }[] {
    const data = this.claim()?.dynamicData;
    if (!data) return [];
    try {
      const parsed = JSON.parse(data);
      return Object.keys(parsed).map(k => ({
        key: k.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase()), // Convert camelCase to Title Case
        value: parsed[k] || 'N/A'
      }));
    } catch (e) {
      return [];
    }
  }
}
