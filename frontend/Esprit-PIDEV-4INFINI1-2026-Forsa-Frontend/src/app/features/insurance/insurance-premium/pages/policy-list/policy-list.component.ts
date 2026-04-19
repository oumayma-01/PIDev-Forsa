import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe, TitleCasePipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { InsurancePolicy } from '../../../shared/models/insurance.models';
import { PolicyStatus } from '../../../shared/enums/insurance.enums';
import type { ForsaIconName } from '../../../../../shared/ui/forsa-icon/forsa-icon.types';

@Component({
  selector: 'app-policy-list',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, TitleCasePipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './policy-list.component.html',
  styleUrl: './policy-list.component.css',
})
export class PolicyListComponent implements OnInit {
  private readonly svc = inject(InsurancePolicyService);

  policies = signal<InsurancePolicy[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  deletingId = signal<number | null>(null);
  reviewingId = signal<number | null>(null);

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.svc.getAll().subscribe({
      next: (data) => { this.policies.set(data); this.loading.set(false); },
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
