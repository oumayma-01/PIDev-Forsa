import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { PremiumPaymentService } from '../../../shared/services/premium-payment.service';
import { PremiumPayment } from '../../../shared/models/insurance.models';
import { PaymentStatus } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-premium-payement-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './premium-payement-detail.component.html',
  styleUrl: './premium-payement-detail.component.css',
})
export class PremiumPayementDetailComponent implements OnInit {
  private readonly svc = inject(PremiumPaymentService);
  private readonly route = inject(ActivatedRoute);

  payment = signal<PremiumPayment | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.error.set('No ID provided'); this.loading.set(false); return; }
    this.svc.getById(+id).subscribe({
      next: (p) => { this.payment.set(p); this.loading.set(false); },
      error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
    });
  }

  statusTone(s?: PaymentStatus): 'success' | 'warning' | 'danger' | 'muted' {
    switch (s) {
      case PaymentStatus.PAID: return 'success';
      case PaymentStatus.PENDING: return 'warning';
      case PaymentStatus.LATE: return 'danger';
      case PaymentStatus.FAILED: return 'danger';
      default: return 'muted';
    }
  }
}
