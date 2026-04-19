import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { InsurancePolicy } from '../../../shared/models/insurance.models';
import { PolicyStatus } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-policy-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './policy-detail.component.html',
  styleUrl: './policy-detail.component.css',
})
export class PolicyDetailComponent implements OnInit {
  private readonly svc = inject(InsurancePolicyService);
  private readonly route = inject(ActivatedRoute);

  policy = signal<InsurancePolicy | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  downloadingPdf = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.error.set('No ID provided'); this.loading.set(false); return; }
    this.svc.getById(+id).subscribe({
      next: (p) => { this.policy.set(p); this.loading.set(false); },
      error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
    });
  }

  downloadPdf(): void {
    const p = this.policy();
    if (!p?.id) return;
    this.downloadingPdf.set(true);
    this.svc.downloadAmortizationPdf(p.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `Amortization_Policy_${p.id}.pdf`; a.click();
        URL.revokeObjectURL(url);
        this.downloadingPdf.set(false);
      },
      error: () => { alert('PDF download failed.'); this.downloadingPdf.set(false); },
    });
  }

  statusTone(s?: PolicyStatus): 'success' | 'warning' | 'danger' | 'info' | 'muted' {
    switch (s) {
      case PolicyStatus.ACTIVE: return 'success';
      case PolicyStatus.PENDING: return 'warning';
      case PolicyStatus.SUSPENDED: return 'info';
      case PolicyStatus.CANCELLED:
      case PolicyStatus.EXPIRED: return 'danger';
      default: return 'muted';
    }
  }
}
