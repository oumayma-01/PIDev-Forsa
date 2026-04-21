import { DecimalPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { AccountService } from '../../../core/services/account.service';
import { AuthService } from '../../../core/services/auth.service';
import type {
  Account,
  Transaction,
  WalletStatisticsDTO,
  WalletForecastDTO,
  AccountTypeAdviceDTO,
} from '../../../core/models/wallet.models';

@Component({
  selector: 'app-wallet-overview',
  standalone: true,
  imports: [DecimalPipe, FormsModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './wallet-overview.component.html',
  styleUrl: './wallet-overview.component.css',
})
export class WalletOverviewComponent implements OnInit {
  account: Account | null = null;
  transactions: Transaction[] = [];
  stats: WalletStatisticsDTO | null = null;
  forecast: WalletForecastDTO | null = null;
  advice: AccountTypeAdviceDTO | null = null;

  loading = true;
  loadingForecast = false;
  loadingAdvice = false;

  showDepositModal = false;
  showTransferModal = false;

  depositAmount: number | null = null;
  transferToAccountId: number | null = null;
  transferAmount: number | null = null;

  operationMessage = '';
  operationError = '';

  constructor(private readonly accountSvc: AccountService, private readonly auth: AuthService) {}

  async ngOnInit(): Promise<void> {
    await this.auth.ensureSessionFromApi();
    const user = this.auth.currentUser();
    if (user?.id) {
      this.accountSvc.getAccountsByOwner(+user.id).subscribe({
        next: (accounts) => {
          if (accounts && accounts.length > 0) {
            this.account = accounts[0];
            this.transactions = accounts[0].wallet?.transactions ?? [];
            this.loadStats();
          }
          this.loading = false;
        },
        error: () => { this.loading = false; },
      });
    } else {
      this.loading = false;
    }
  }

  get balance(): number {
    return this.account?.wallet?.balance ?? 0;
  }

  isIncoming(tx: Transaction): boolean {
    return tx.type === 'DEPOSIT' || tx.type === 'TRANSFER_IN' || tx.type === 'INTEREST';
  }

  txLabel(tx: Transaction): string {
    switch (tx.type) {
      case 'DEPOSIT':      return 'Deposit';
      case 'WITHDRAW':     return 'Withdrawal';
      case 'TRANSFER_IN':  return 'Transfer In';
      case 'TRANSFER_OUT': return 'Transfer Out';
      case 'INTEREST':     return 'Interest';
      default:             return tx.type;
    }
  }

  formatDate(date: string): string {
    if (!date) return '';
    return date.substring(0, 10);
  }

  openDepositModal(): void {
    this.depositAmount = null;
    this.operationMessage = '';
    this.operationError = '';
    this.showDepositModal = true;
  }

  closeDepositModal(): void {
    this.showDepositModal = false;
  }

  submitDeposit(): void {
    if (!this.account || !this.depositAmount || this.depositAmount <= 0) return;
    const amount = this.depositAmount;
    this.accountSvc.deposit(this.account.id, amount).subscribe({
      next: () => {
        this.operationMessage = `Successfully deposited $${amount.toFixed(2)}`;
        this.showDepositModal = false;
        this.refreshAccount();
      },
      error: () => { this.operationError = 'Deposit failed. Please try again.'; },
    });
  }

  openTransferModal(): void {
    this.transferToAccountId = null;
    this.transferAmount = null;
    this.operationMessage = '';
    this.operationError = '';
    this.showTransferModal = true;
  }

  closeTransferModal(): void {
    this.showTransferModal = false;
  }

  submitTransfer(): void {
    if (!this.account || !this.transferToAccountId || !this.transferAmount || this.transferAmount <= 0) return;
    const amount = this.transferAmount;
    this.accountSvc.transfer(this.account.id, this.transferToAccountId, amount).subscribe({
      next: () => {
        this.operationMessage = `Successfully transferred $${amount.toFixed(2)}`;
        this.showTransferModal = false;
        this.refreshAccount();
      },
      error: () => { this.operationError = 'Transfer failed. Please check the account ID and try again.'; },
    });
  }

  loadForecast(): void {
    if (!this.account) return;
    this.loadingForecast = true;
    this.forecast = null;
    this.accountSvc.forecast(this.account.id, 30).subscribe({
      next: (f) => { this.forecast = f; this.loadingForecast = false; },
      error: () => { this.loadingForecast = false; },
    });
  }

  loadAdvice(): void {
    if (!this.account) return;
    this.loadingAdvice = true;
    this.advice = null;
    this.accountSvc.getAccountTypeAdvice(this.account.id).subscribe({
      next: (a) => { this.advice = a; this.loadingAdvice = false; },
      error: () => { this.loadingAdvice = false; },
    });
  }

  private loadStats(): void {
    if (!this.account) return;
    this.accountSvc.getStatistics(this.account.id).subscribe({
      next: (s) => (this.stats = s),
      error: () => {},
    });
  }

  private refreshAccount(): void {
    if (!this.account) return;
    this.accountSvc.getAccount(this.account.id).subscribe({
      next: (acc) => {
        this.account = acc;
        this.transactions = acc.wallet?.transactions ?? [];
        this.loadStats();
      },
    });
  }
}
