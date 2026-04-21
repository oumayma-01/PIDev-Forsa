import { DecimalPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { MOCK_TRANSACTIONS } from '../../../core/data/mock-data';
import { AccountService } from '../../../core/services/account.service';
import { AuthService } from '../../../core/services/auth.service';
import type { Transaction } from '../../../core/models/wallet.models';

@Component({
  selector: 'app-wallet-overview',
  standalone: true,
  imports: [DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './wallet-overview.component.html',
  styleUrl: './wallet-overview.component.css',
})
export class WalletOverviewComponent implements OnInit {
  transactions: Transaction[] = [];

  constructor(private readonly account: AccountService, private readonly auth: AuthService) {}

  async ngOnInit(): Promise<void> {
    // Ensure session loaded; if not available, fall back to mock data
    await this.auth.ensureSessionFromApi();
    const user = this.auth.currentUser();
    if (user?.id) {
      this.account.getAccountsByOwner(user.id).subscribe((accounts) => {
        if (accounts && accounts.length > 0 && accounts[0].wallet?.transactions) {
          this.transactions = accounts[0].wallet.transactions;
        } else {
          this.transactions = MOCK_TRANSACTIONS;
        }
      }, () => (this.transactions = MOCK_TRANSACTIONS));
    } else {
      this.transactions = MOCK_TRANSACTIONS;
    }
  }
}
