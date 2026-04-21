import { Component, Input, OnChanges, SimpleChanges, signal } from '@angular/core';
import { RepaymentScheduleApi, RepaymentStatus } from '../../../core/models/credit-api.model';
import { DecimalPipe, DatePipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';

@Component({
  selector: 'app-repayment-schedule-list',
  standalone: true,
  imports: [DecimalPipe, DatePipe, ForsaBadgeComponent],
  templateUrl: './repayment-schedule-list.component.html',
  styleUrl: './repayment-schedule-list.component.css'
})
export class RepaymentScheduleListComponent implements OnChanges {
  @Input({ required: true }) repayments: RepaymentScheduleApi[] = [];
  @Input() loading = false;
  @Input() error: string | null = null;

  // Internal signals to drive the template
  readonly localRepayments = signal<RepaymentScheduleApi[]>([]);
  readonly localLoading = signal<boolean>(false);
  readonly localError = signal<string | null>(null);

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['repayments']) {
      this.localRepayments.set(this.repayments);
    }
    if (changes['loading']) {
      this.localLoading.set(this.loading);
    }
    if (changes['error']) {
      this.localError.set(this.error);
    }
  }

  statusTone(status: RepaymentStatus): 'success' | 'warning' | 'danger' | 'muted' {
    switch (status) {
      case 'PAID':
        return 'success';
      case 'LATE':
        return 'danger';
      case 'PENDING':
      default:
        return 'muted';
    }
  }
}
