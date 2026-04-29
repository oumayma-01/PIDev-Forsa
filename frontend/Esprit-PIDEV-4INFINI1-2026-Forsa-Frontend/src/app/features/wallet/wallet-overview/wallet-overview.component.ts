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
  get isAgent(): boolean { return this.auth.currentUser()?.roles?.includes('ROLE_AGENT') ?? false; }
  get isClient(): boolean { return this.auth.currentUser()?.roles?.includes('ROLE_CLIENT') ?? false; }
  get isStaff(): boolean { return this.isAdmin || this.isAgent; }

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
  adminSearchTerm = '';
  adminTypeFilter: '' | 'SAVINGS' | 'INVESTMENT' = '';
  adminStatusFilter: '' | 'ACTIVE' | 'BLOCKED' = '';

  bankVaultTotal: number | null = null;
  loadingVault = false;

  staffLookupAccountId: number | null = null;
  staffLookupAccount: Account | null = null;
  staffLookupStats: WalletStatisticsDTO | null = null;
  staffLookupActivities: Activity[] = [];
  loadingStaffLookup = false;

  // ── Loading flags ─────────────────────────────────────────────────────────────
  loading = true;
  loadingForecast = false;
  loadingAdvice = false;
  loadingAdaptive = false;
  loadingActivities = false;
  loadingAdminOp = false;

  // ── Modals ────────────────────────────────────────────────────────────────────
  showCreateModal = false;
  showDepositModal = false;
  showWithdrawModal = false;
  showTransferModal = false;
  showActivitiesModal = false;
  showAdminCreateModal = false;
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
  adminCreateOwnerId: number | null = null;
  adminCreateType: 'SAVINGS' | 'INVESTMENT' = 'SAVINGS';
  adminCreateHolderName = '';

  // ── Feedback ──────────────────────────────────────────────────────────────────
  operationMessage = '';
  operationError = '';

  constructor(private readonly accountSvc: AccountService, private readonly auth: AuthService) {}

  async ngOnInit(): Promise<void> {
    await this.auth.ensureSessionFromApi();
    if (this.isStaff) {
      this.loadBankVault();
      if (this.isAdmin) {
        this.loadAllAccounts();
      }
      this.loading = false;
      return;
    }
    this.loadClientAccount();
  }

  // ── STAFF methods (ADMIN + AGENT) ────────────────────────────────────────────

  loadBankVault(): void {
    this.loadingVault = true;
    this.accountSvc.getBankVault().subscribe({
      next: (v) => {
        this.bankVaultTotal = Number(v.totalFunds ?? 0);
        this.loadingVault = false;
      },
      error: () => {
        this.bankVaultTotal = null;
        this.loadingVault = false;
      },
    });
  }

  staffLookup(): void {
    const id = this.staffLookupAccountId;
    if (!id || id <= 0) {
      return;
    }
    this.operationMessage = '';
    this.operationError = '';
    this.loadingStaffLookup = true;
    this.staffLookupAccount = null;
    this.staffLookupStats = null;
    this.staffLookupActivities = [];

    this.accountSvc.getAccount(id).subscribe({
      next: (acc) => {
        this.staffLookupAccount = acc;
        this.accountSvc.getStatistics(id).subscribe({ next: (s) => (this.staffLookupStats = s) });
        this.accountSvc.getActivities(id).subscribe({ next: (a) => (this.staffLookupActivities = a) });
        this.loadingStaffLookup = false;
      },
      error: (e) => {
        this.operationError = e?.error?.message || e?.error || 'Account not found.';
        this.loadingStaffLookup = false;
      },
    });
  }

  // ── ADMIN computed stats ──────────────────────────────────────────────────────

  get adminTotalBalance(): number {
    return this.allAccounts.reduce((s, a) => s + (a.wallet?.balance ?? 0), 0);
  }
  get adminActiveCount(): number { return this.allAccounts.filter(a => a.status === 'ACTIVE').length; }
  get adminBlockedCount(): number { return this.allAccounts.filter(a => a.status === 'BLOCKED').length; }
  get adminInvestmentCount(): number { return this.allAccounts.filter(a => a.type === 'INVESTMENT').length; }

  get filteredAccounts(): Account[] {
    return this.allAccounts.filter(a => {
      const term = this.adminSearchTerm.toLowerCase();
      const matchSearch = !term ||
        String(a.id).includes(term) ||
        (a.accountHolderName ?? '').toLowerCase().includes(term);
      const matchType = !this.adminTypeFilter || a.type === this.adminTypeFilter;
      const matchStatus = !this.adminStatusFilter || a.status === this.adminStatusFilter;
      return matchSearch && matchType && matchStatus;
    });
  }

  // ── ADMIN methods ─────────────────────────────────────────────────────────────

  loadAllAccounts(): void {
    if (!this.isAdmin) {
      return;
    }
    this.loading = true;
    this.accountSvc.getAllAccounts().subscribe({
      next: (list) => { this.allAccounts = list; this.loading = false; },
      error: () => { this.loading = false; },
    });
  }

  selectAdminAccount(acc: Account): void {
    if (!this.isAdmin) {
      return;
    }
    this.adminSelectedAccount = acc;
    this.adminStats = null;
    this.operationMessage = '';
    this.operationError = '';
    this.accountSvc.getStatistics(acc.id).subscribe({ next: (s) => (this.adminStats = s) });
  }

  openAdminCreate(): void {
    if (!this.isAdmin) {
      return;
    }
    this.adminCreateOwnerId = null;
    this.adminCreateType = 'SAVINGS';
    this.adminCreateHolderName = '';
    this.operationError = '';
    this.showAdminCreateModal = true;
  }

  submitAdminCreate(): void {
    if (!this.isAdmin) {
      return;
    }
    if (!this.adminCreateOwnerId || this.adminCreateOwnerId <= 0) return;
    this.loadingAdminOp = true;
    this.accountSvc.createAccount(this.adminCreateOwnerId, this.adminCreateType, this.adminCreateHolderName || undefined).subscribe({
      next: (acc) => {
        this.allAccounts = [...this.allAccounts, acc];
        this.operationMessage = `Account #${acc.id} created for user ${this.adminCreateOwnerId}.`;
        this.showAdminCreateModal = false;
        this.loadingAdminOp = false;
      },
      error: (e) => { this.operationError = e?.error?.message || e?.error || 'Creation failed. User ID not found in the database.'; this.loadingAdminOp = false; },
    });
  }

  adminDeposit(): void {
    if (!this.isAdmin) {
      return;
    }
    if (!this.adminSelectedAccount || !this.adminDepositAmount || this.adminDepositAmount <= 0) return;
    const amount = this.adminDepositAmount;
    this.loadingAdminOp = true;
    this.accountSvc.deposit(this.adminSelectedAccount.id, amount).subscribe({
      next: () => {
        this.operationMessage = `Deposited $${amount.toFixed(2)} to account #${this.adminSelectedAccount!.id}`;
        this.showAdminDepositModal = false;
        this.loadingAdminOp = false;
        this.refreshAdminAccount(this.adminSelectedAccount!.id);
      },
      error: () => { this.operationError = 'Deposit failed.'; this.loadingAdminOp = false; },
    });
  }

  adminWithdraw(): void {
    if (!this.isAdmin) {
      return;
    }
    if (!this.adminSelectedAccount || !this.adminWithdrawAmount || this.adminWithdrawAmount <= 0) return;
    const amount = this.adminWithdrawAmount;
    this.loadingAdminOp = true;
    this.accountSvc.withdraw(this.adminSelectedAccount.id, amount).subscribe({
      next: () => {
        this.operationMessage = `Withdrew $${amount.toFixed(2)} from account #${this.adminSelectedAccount!.id}`;
        this.showAdminWithdrawModal = false;
        this.loadingAdminOp = false;
        this.refreshAdminAccount(this.adminSelectedAccount!.id);
      },
      error: () => { this.operationError = 'Withdrawal failed (insufficient balance?).'; this.loadingAdminOp = false; },
    });
  }

  toggleStatus(acc: Account, event: Event): void {
    if (!this.isAdmin) {
      return;
    }
    event.stopPropagation();
    const next = acc.status === 'ACTIVE' ? 'BLOCKED' : 'ACTIVE';
    this.accountSvc.updateAccountStatus(acc.id, next).subscribe({
      next: (updated) => {
        const idx = this.allAccounts.findIndex(a => a.id === updated.id);
        if (idx !== -1) this.allAccounts[idx] = { ...this.allAccounts[idx], status: updated.status };
        if (this.adminSelectedAccount?.id === updated.id) this.adminSelectedAccount = { ...this.adminSelectedAccount, status: updated.status };
        this.operationMessage = `Account #${updated.id} is now ${updated.status}.`;
      },
      error: () => { this.operationError = 'Status update failed.'; },
    });
  }

  deleteAccount(acc: Account, event: Event): void {
    if (!this.isAdmin) {
      return;
    }
    event.stopPropagation();
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
    if (!this.isAdmin) {
      return;
    }
    this.accountSvc.applyMonthlyInterest().subscribe({
      next: () => {
        this.operationMessage = 'Monthly interest applied to all active INVESTMENT accounts.';
        this.loadAllAccounts();
      },
      error: () => { this.operationError = 'No active INVESTMENT accounts found or request failed.'; },
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

  openCreateModal(): void { this.createType = 'SAVINGS'; this.operationError = ''; this.showCreateModal = true; }
  closeCreateModal(): void { this.showCreateModal = false; }

  submitCreate(): void {
    const user = this.auth.currentUser();
    if (!user?.id) return;
    const holderName = user.username ?? `User #${user.id}`;
    this.accountSvc.createAccount(+user.id, this.createType, holderName).subscribe({
      next: (acc) => {
        this.account = acc;
        this.transactions = [];
        this.showCreateModal = false;
        this.operationMessage = `${this.createType} account created successfully!`;
        this.loadStats();
      },
      error: () => { this.operationError = 'Account creation failed. Please try again.'; },
    });
  }

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

  openWithdrawModal(): void { this.withdrawAmount = null; this.operationError = ''; this.showWithdrawModal = true; }
  closeWithdrawModal(): void { this.showWithdrawModal = false; }
  submitWithdraw(): void {
    if (!this.account || !this.withdrawAmount || this.withdrawAmount <= 0) return;
    const amount = this.withdrawAmount;
    this.accountSvc.withdraw(this.account.id, amount).subscribe({
      next: () => { this.operationMessage = `Withdrew $${amount.toFixed(2)}`; this.showWithdrawModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Insufficient balance or account is blocked.'; },
    });
  }

  openTransferModal(): void { this.transferToAccountId = null; this.transferAmount = null; this.operationError = ''; this.showTransferModal = true; }
  closeTransferModal(): void { this.showTransferModal = false; }
  submitTransfer(): void {
    if (!this.account || !this.transferToAccountId || !this.transferAmount || this.transferAmount <= 0) return;
    const amount = this.transferAmount;
    this.accountSvc.transfer(this.account.id, this.transferToAccountId, amount).subscribe({
      next: () => { this.operationMessage = `Transferred $${amount.toFixed(2)}`; this.showTransferModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Transfer failed. Check the account ID and your balance.'; },
    });
  }

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
      error: () => { this.operationError = 'Smart interest only applies to INVESTMENT accounts.'; this.loadingAdaptive = false; },
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
