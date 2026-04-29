import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { PremiumPaymentService } from '../../../shared/services/premium-payment.service';
import { PremiumPayment } from '../../../shared/models/insurance.models';
import { PaymentStatus } from '../../../shared/enums/insurance.enums';
import type { ForsaIconName } from '../../../../../shared/ui/forsa-icon/forsa-icon.types';

import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-premium-payement-list',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, FormsModule],
  templateUrl: './premium-payement-list.component.html',
  styleUrl: './premium-payement-list.component.css',
})
export class PremiumPayementListComponent implements OnInit {
  private readonly svc = inject(PremiumPaymentService);

  payments = signal<PremiumPayment[]>([]);
  searchTerm = signal('');
  statusFilter = signal<string>('ALL');
  loading = signal(true);
  error = signal<string | null>(null);
  deletingId = signal<number | null>(null);

  filteredPayments = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const status = this.statusFilter();
    
    return this.payments().filter(p => {
      const matchesSearch = !term || 
        p.insurancePolicy?.policyNumber?.toLowerCase().includes(term);
        
      const matchesStatus = status === 'ALL' || p.status === status;
      
      return matchesSearch && matchesStatus;
    });
  });

  PaymentStatus = PaymentStatus; // Expose to template

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: (data) => { this.payments.set(data); this.loading.set(false); },
      error: (e) => { this.error.set(e.message ?? 'Failed to load payments.'); this.loading.set(false); },
    });
  }

  delete(id: number): void {
    if (!confirm('Delete this payment?')) return;
    this.deletingId.set(id);
    this.svc.delete(id).subscribe({
      next: () => { this.payments.update((p) => p.filter((x) => x.id !== id)); this.deletingId.set(null); },
      error: (e) => { alert(e.message ?? 'Delete failed.'); this.deletingId.set(null); },
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

  statusIcon(s?: PaymentStatus): ForsaIconName {
    switch (s) {
      case PaymentStatus.PAID: return 'check-circle-2';
      case PaymentStatus.PENDING: return 'clock';
      case PaymentStatus.LATE: return 'alert-circle';
      case PaymentStatus.FAILED: return 'alert-circle';
      default: return 'history';
    }
  }
}
