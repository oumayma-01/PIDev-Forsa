import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import type { ClientCashback, PartnerTransaction } from '../../../core/models/forsa.models';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { PartnerService } from '../services/partner.service';

@Component({
  selector: 'app-client-transactions',
  standalone: true,
  imports: [DatePipe, DecimalPipe, RouterLink, ForsaCardComponent, ForsaButtonComponent, ForsaIconComponent, ForsaBadgeComponent],
  templateUrl: './client-transactions.component.html',
  styleUrl: './client-transactions.component.css',
})
export class ClientTransactionsComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly partnerService = inject(PartnerService);

  transactions = signal<PartnerTransaction[]>([]);
  cashback = signal<ClientCashback | null>(null);
  loading = signal(true);
  cashbackLoading = signal(true);
  redeeming = signal(false);

  ngOnInit(): void {
    const user = this.auth.currentUser();
    if (!user?.id) return;
    const id = Number(user.id);
    this.partnerService.getClientTransactions(id).subscribe({
      next: (t: PartnerTransaction[]) => { this.transactions.set(t); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.partnerService.getClientCashback(id).subscribe({
      next: (c: ClientCashback) => { this.cashback.set(c); this.cashbackLoading.set(false); },
      error: () => this.cashbackLoading.set(false),
    });
  }

  redeemCashback(): void {
    const user = this.auth.currentUser();
    const cb = this.cashback();
    if (!user?.id || !cb || cb.availableBalance <= 0) return;
    this.redeeming.set(true);
    this.partnerService.redeemCashback(Number(user.id), cb.availableBalance).subscribe({
      next: (updated: ClientCashback) => { this.cashback.set(updated); this.redeeming.set(false); },
      error: () => this.redeeming.set(false),
    });
  }

  statusTone(status: PartnerTransaction['status']): 'success' | 'warning' | 'danger' | 'muted' {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'PENDING':   return 'warning';
      case 'FAILED':    return 'danger';
      case 'CANCELLED': return 'muted';
    }
  }
}