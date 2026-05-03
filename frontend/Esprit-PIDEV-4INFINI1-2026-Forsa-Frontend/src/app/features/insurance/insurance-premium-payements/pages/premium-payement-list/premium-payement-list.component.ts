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
import { PremiumPaymentService } from '../../../shared/services/premium-payment.service';
import { PremiumPayment } from '../../../shared/models/insurance.models';
import { PaymentStatus } from '../../../shared/enums/insurance.enums';
import type { ForsaIconName } from '../../../../../shared/ui/forsa-icon/forsa-icon.types';

import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-premium-payement-list',
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

    return this.payments().filter((p) => {
      const matchesSearch = !term || p.insurancePolicy?.policyNumber?.toLowerCase().includes(term);

      const matchesStatus = status === 'ALL' || p.status === status;

      return matchesSearch && matchesStatus;
    });
  });

  readonly paymentTablePageIndex = signal(0);
  readonly paymentTablePageSize = signal(10);
  paymentTablePageSizeOptions: number[] = [5, 10, 25, 50];

  readonly paymentTableColumns: ForsaTableColumn[] = [
    { key: 'id', label: 'ID', width: '5rem' },
    { key: 'policy', label: 'Policy', width: '9rem' },
    { key: 'status', label: 'Status', width: '9rem' },
    { key: 'amount', label: 'Amount', align: 'right', width: '8rem' },
    { key: 'due', label: 'Due date', width: '8rem' },
    { key: 'paid', label: 'Paid date', width: '8rem' },
    { key: 'tx', label: 'Transaction ID' },
    { key: 'actions', label: 'Actions', align: 'right', width: '9rem' },
  ];

  readonly pagedPayments = computed(() => {
    const list = this.filteredPayments();
    const start = this.paymentTablePageIndex() * this.paymentTablePageSize();
    return list.slice(start, start + this.paymentTablePageSize());
  });

  PaymentStatus = PaymentStatus; // Expose to template

  constructor() {
    effect(() => {
      this.searchTerm();
      this.statusFilter();
      untracked(() => this.paymentTablePageIndex.set(0));
    });
    effect(
      () => {
        const total = this.filteredPayments().length;
        const sz = this.paymentTablePageSize();
        const maxIdx = Math.max(0, Math.ceil(total / sz) - 1);
        untracked(() => {
          if (this.paymentTablePageIndex() > maxIdx) {
            this.paymentTablePageIndex.set(maxIdx);
          }
        });
      },
      { allowSignalWrites: true },
    );
  }

  ngOnInit(): void {
    this.load();
  }

  onPaymentTablePage(ev: ForsaDataTablePageEvent): void {
    this.paymentTablePageIndex.set(ev.pageIndex);
    this.paymentTablePageSize.set(ev.pageSize);
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: (data) => {
        this.payments.set(data);
        this.paymentTablePageIndex.set(0);
        this.loading.set(false);
      },
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
      case PaymentStatus.PAID:
        return 'success';
      case PaymentStatus.PENDING:
        return 'warning';
      case PaymentStatus.OVERDUE:
      case PaymentStatus.LATE:
      case PaymentStatus.FAILED:
        return 'danger';
      default:
        return 'muted';
    }
  }

  statusIcon(s?: PaymentStatus): ForsaIconName {
    switch (s) {
      case PaymentStatus.PAID:
        return 'check-circle-2';
      case PaymentStatus.PENDING:
        return 'clock';
      case PaymentStatus.OVERDUE:
      case PaymentStatus.LATE:
      case PaymentStatus.FAILED:
        return 'alert-circle';
      default:
        return 'history';
    }
  }
}
