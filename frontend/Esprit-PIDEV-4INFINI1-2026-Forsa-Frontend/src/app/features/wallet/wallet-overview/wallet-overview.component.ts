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
  Activity,
  Transaction,
  WalletStatisticsDTO,
  WalletForecastDTO,
  AccountTypeAdviceDTO,
  AdaptiveInterestResultDTO,
} from '../../../core/models/wallet.models';

@Component({
  selector: 'app-wallet-overview',
  standalone: true,
  imports: [DecimalPipe, FormsModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './wallet-overview.component.html',
  styleUrl: './wallet-overview.component.css',
})
export class WalletOverviewComponent implements OnInit {

  // ── Auth ─────────────────────────────────────────────────────────────────────
  get isAdmin(): boolean { return this.auth.currentUser()?.roles?.includes('ROLE_ADMIN') ?? false; }
  get isClient(): boolean { return !this.isAdmin; }

  // ── CLIENT state ─────────────────────────────────────────────────────────────
  account: Account | null = null;
  transactions: Transaction[] = [];
  activities: Activity[] = [];
  stats: WalletStatisticsDTO | null = null;
  forecast: WalletForecastDTO | null = null;
  advice: AccountTypeAdviceDTO | null = null;
  adaptiveResult: AdaptiveInterestResultDTO | null = null;

  // ── ADMIN state ───────────────────────────────────────────────────────────────
  allAccounts: Account[] = [];
  adminSelectedAccount: Account | null = null;
  adminStats: WalletStatisticsDTO | null = null;

  // ── Loading flags ─────────────────────────────────────────────────────────────
  loading = true;
  loadingForecast = false;
  loadingAdvice = false;
  loadingAdaptive = false;
  loadingActivities = false;

  // ── Modals ────────────────────────────────────────────────────────────────────
  showCreateModal = false;
  showDepositModal = false;
  showWithdrawModal = false;
  showTransferModal = false;
  showActivitiesModal = false;

  // ── Admin modals ──────────────────────────────────────────────────────────────
  showAdminDepositModal = false;
  showAdminWithdrawModal = false;

  // ── Form fields ───────────────────────────────────────────────────────────────
  createType: 'SAVINGS' | 'INVESTMENT' = 'SAVINGS';
  depositAmount: number | null = null;
  withdrawAmount: number | null = null;
  transferToAccountId: number | null = null;
  transferAmount: number | null = null;
  adminDepositAmount: number | null = null;
  adminWithdrawAmount: number | null = null;

  // ── Feedback ──────────────────────────────────────────────────────────────────
  operationMessage = '';
  operationError = '';

  constructor(private readonly accountSvc: AccountService, private readonly auth: AuthService) {}

  async ngOnInit(): Promise<void> {
    await this.auth.ensureSessionFromApi();
    if (this.isAdmin) {
      this.loadAllAccounts();
    } else {
      this.loadClientAccount();
    }
  }

  // ── ADMIN methods ─────────────────────────────────────────────────────────────

  loadAllAccounts(): void {
    this.loading = true;
    this.accountSvc.getAllAccounts().subscribe({
      next: (list) => { this.allAccounts = list; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  selectAdminAccount(acc: Account): void {
    this.adminSelectedAccount = acc;
    this.adminStats = null;
    this.adminDepositAmount = null;
    this.adminWithdrawAmount = null;
    this.operationMessage = '';
    this.operationError = '';
    this.accountSvc.getStatistics(acc.id).subscribe({ next: (s) => (this.adminStats = s) });
  }

  adminDeposit(): void {
    if (!this.adminSelectedAccount || !this.adminDepositAmount || this.adminDepositAmount <= 0) return;
    const amount = this.adminDepositAmount;
    this.accountSvc.deposit(this.adminSelectedAccount.id, amount).subscribe({
      next: () => {
        this.operationMessage = `Deposited $${amount.toFixed(2)} to account #${this.adminSelectedAccount!.id}`;
        this.showAdminDepositModal = false;
        this.refreshAdminAccount(this.adminSelectedAccount!.id);
      },
      error: () => { this.operationError = 'Deposit failed.'; },
    });
  }

  adminWithdraw(): void {
    if (!this.adminSelectedAccount || !this.adminWithdrawAmount || this.adminWithdrawAmount <= 0) return;
    const amount = this.adminWithdrawAmount;
    this.accountSvc.withdraw(this.adminSelectedAccount.id, amount).subscribe({
      next: () => {
        this.operationMessage = `Withdrew $${amount.toFixed(2)} from account #${this.adminSelectedAccount!.id}`;
        this.showAdminWithdrawModal = false;
        this.refreshAdminAccount(this.adminSelectedAccount!.id);
      },
      error: () => { this.operationError = 'Withdrawal failed.'; },
    });
  }

  toggleStatus(acc: Account): void {
    const next = acc.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
    this.accountSvc.updateAccountStatus(acc.id, next).subscribe({
      next: (updated) => {
        const idx = this.allAccounts.findIndex(a => a.id === updated.id);
        if (idx !== -1) this.allAccounts[idx] = updated;
        if (this.adminSelectedAccount?.id === updated.id) this.adminSelectedAccount = updated;
        this.operationMessage = `Account #${updated.id} is now ${updated.status}`;
      },
      error: () => { this.operationError = 'Status update failed.'; },
    });
  }

  deleteAccount(acc: Account): void {
    if (!confirm(`Delete account #${acc.id}? This cannot be undone.`)) return;
    this.accountSvc.deleteAccount(acc.id).subscribe({
      next: () => {
        this.allAccounts = this.allAccounts.filter(a => a.id !== acc.id);
        if (this.adminSelectedAccount?.id === acc.id) this.adminSelectedAccount = null;
        this.operationMessage = `Account #${acc.id} deleted.`;
      },
      error: () => { this.operationError = 'Delete failed.'; },
    });
  }

  applyMonthlyInterest(): void {
    this.accountSvc.applyMonthlyInterest().subscribe({
      next: () => {
        this.operationMessage = 'Monthly interest applied to all INVESTMENT accounts.';
        this.loadAllAccounts();
      },
      error: () => { this.operationError = 'Failed to apply interest.'; },
    });
  }

  private refreshAdminAccount(id: number): void {
    this.accountSvc.getAccount(id).subscribe({
      next: (acc) => {
        const idx = this.allAccounts.findIndex(a => a.id === id);
        if (idx !== -1) this.allAccounts[idx] = acc;
        if (this.adminSelectedAccount?.id === id) {
          this.adminSelectedAccount = acc;
          this.accountSvc.getStatistics(id).subscribe({ next: (s) => (this.adminStats = s) });
        }
      },
    });
  }

  // ── CLIENT methods ────────────────────────────────────────────────────────────

  private loadClientAccount(): void {
    const user = this.auth.currentUser();
    if (!user?.id) { this.loading = false; return; }
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
  }

  get balance(): number { return this.account?.wallet?.balance ?? 0; }

  // Create account
  openCreateModal(): void { this.createType = 'SAVINGS'; this.operationError = ''; this.showCreateModal = true; }
  closeCreateModal(): void { this.showCreateModal = false; }
  submitCreate(): void {
    const user = this.auth.currentUser();
    if (!user?.id) return;
    this.accountSvc.createAccount(+user.id, this.createType).subscribe({
      next: (acc) => {
        this.account = acc;
        this.transactions = [];
        this.showCreateModal = false;
        this.operationMessage = `${this.createType} account created successfully!`;
        this.loadStats();
      },
      error: () => { this.operationError = 'Account creation failed.'; },
    });
  }

  // Deposit
  openDepositModal(): void { this.depositAmount = null; this.operationError = ''; this.showDepositModal = true; }
  closeDepositModal(): void { this.showDepositModal = false; }
  submitDeposit(): void {
    if (!this.account || !this.depositAmount || this.depositAmount <= 0) return;
    const amount = this.depositAmount;
    this.accountSvc.deposit(this.account.id, amount).subscribe({
      next: () => { this.operationMessage = `Deposited $${amount.toFixed(2)}`; this.showDepositModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Deposit failed.'; },
    });
  }

  // Withdraw
  openWithdrawModal(): void { this.withdrawAmount = null; this.operationError = ''; this.showWithdrawModal = true; }
  closeWithdrawModal(): void { this.showWithdrawModal = false; }
  submitWithdraw(): void {
    if (!this.account || !this.withdrawAmount || this.withdrawAmount <= 0) return;
    const amount = this.withdrawAmount;
    this.accountSvc.withdraw(this.account.id, amount).subscribe({
      next: () => { this.operationMessage = `Withdrew $${amount.toFixed(2)}`; this.showWithdrawModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Insufficient balance or account blocked.'; },
    });
  }

  // Transfer
  openTransferModal(): void { this.transferToAccountId = null; this.transferAmount = null; this.operationError = ''; this.showTransferModal = true; }
  closeTransferModal(): void { this.showTransferModal = false; }
  submitTransfer(): void {
    if (!this.account || !this.transferToAccountId || !this.transferAmount || this.transferAmount <= 0) return;
    const amount = this.transferAmount;
    this.accountSvc.transfer(this.account.id, this.transferToAccountId, amount).subscribe({
      next: () => { this.operationMessage = `Transferred $${amount.toFixed(2)}`; this.showTransferModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Transfer failed. Check account ID and balance.'; },
    });
  }

  // Activities
  openActivities(): void {
    if (!this.account) return;
    this.showActivitiesModal = true;
    this.loadingActivities = true;
    this.accountSvc.getActivities(this.account.id).subscribe({
      next: (a) => { this.activities = a; this.loadingActivities = false; },
      error: () => { this.loadingActivities = false; },
    });
  }
  closeActivities(): void { this.showActivitiesModal = false; }

  // AI
  loadForecast(): void {
    if (!this.account) return;
    this.loadingForecast = true; this.forecast = null;
    this.accountSvc.forecast(this.account.id, 30).subscribe({
      next: (f) => { this.forecast = f; this.loadingForecast = false; },
      error: () => { this.loadingForecast = false; },
    });
  }

  loadAdvice(): void {
    if (!this.account) return;
    this.loadingAdvice = true; this.advice = null;
    this.accountSvc.getAccountTypeAdvice(this.account.id).subscribe({
      next: (a) => { this.advice = a; this.loadingAdvice = false; },
      error: () => { this.loadingAdvice = false; },
    });
  }

  applyAdaptiveInterest(): void {
    if (!this.account) return;
    this.loadingAdaptive = true; this.adaptiveResult = null;
    this.accountSvc.applyAdaptiveInterest(this.account.id).subscribe({
      next: (r) => { this.adaptiveResult = r; this.loadingAdaptive = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Adaptive interest failed (INVESTMENT accounts only).'; this.loadingAdaptive = false; },
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  isIncoming(tx: Transaction): boolean {
    return tx.type === 'DEPOSIT' || tx.type === 'TRANSFER_IN' || tx.type === 'INTEREST';
  }

  txLabel(tx: Transaction): string {
    const map: Record<string, string> = {
      DEPOSIT: 'Deposit', WITHDRAW: 'Withdrawal',
      TRANSFER_IN: 'Transfer In', TRANSFER_OUT: 'Transfer Out', INTEREST: 'Interest',
    };
    return map[tx.type] ?? tx.type;
  }

  formatDate(date: string): string { return date ? date.substring(0, 10) : ''; }

  clearMessage(): void { this.operationMessage = ''; this.operationError = ''; }

  private loadStats(): void {
    if (!this.account) return;
    this.accountSvc.getStatistics(this.account.id).subscribe({ next: (s) => (this.stats = s) });
  }

  private refreshClientAccount(): void {
    if (!this.account) return;
    this.accountSvc.getAccount(this.account.id).subscribe({
      next: (acc) => { this.account = acc; this.transactions = acc.wallet?.transactions ?? []; this.loadStats(); },
    });
  }
}
