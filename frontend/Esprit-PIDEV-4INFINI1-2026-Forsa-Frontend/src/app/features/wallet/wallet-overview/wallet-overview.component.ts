import { DecimalPipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
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
  investmentAccount: Account | null = null;
  /** Which wallet the client is viewing when they have both SAVINGS and INVESTMENT. */
  clientWalletView: 'SAVINGS' | 'INVESTMENT' = 'SAVINGS';
  transactions: Transaction[] = [];
  activities: Activity[] = [];
  stats: WalletStatisticsDTO | null = null;
  forecast: WalletForecastDTO | null = null;
  advice: AccountTypeAdviceDTO | null = null;

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
  showInvestmentCreateModal = false;

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
  /** Set when GET /accounts/owner/:id fails (e.g. JSON parse) — do not show "create account" in this state. */
  clientAccountsLoadError: string | null = null;

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
    this.loadClientAccounts();
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
      next: (list) => {
        this.allAccounts = list;
        this.loading = false;
        this.operationError = '';
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.operationError = this.httpErrorMessage(err, 'Could not load accounts list.');
      },
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
    // List payloads omit transactions; load full account for the detail panel.
    this.accountSvc.getAccount(acc.id).subscribe({
      next: (full) => {
        if (this.adminSelectedAccount?.id === full.id) {
          this.adminSelectedAccount = full;
        }
      },
    });
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
    this.operationError = '';
    this.accountSvc.applyMonthlyInterest().subscribe({
      next: () => {
        this.operationMessage =
          'Monthly interest run finished. Every ACTIVE INVESTMENT account with a positive balance was credited (vault updated).';
        this.loadBankVault();
        this.loadAllAccounts();
      },
      error: (err: HttpErrorResponse) => {
        this.operationError = this.httpErrorMessage(err, 'Could not apply monthly interest.');
      },
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

  get hasClientWallet(): boolean {
    return !!(this.account || this.investmentAccount);
  }

  /** The account currently shown in the client hero (savings and/or investment switcher). */
  get activeClientAccount(): Account | null {
    if (this.clientWalletView === 'INVESTMENT' && this.investmentAccount) {
      return this.investmentAccount;
    }
    return this.account;
  }

  selectClientWalletView(view: 'SAVINGS' | 'INVESTMENT'): void {
    if (view === 'INVESTMENT' && !this.investmentAccount) return;
    this.clientWalletView = view;
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.accountSvc.getAccount(acc.id).subscribe({
      next: (fresh) => {
        if (fresh.type === 'INVESTMENT') this.investmentAccount = fresh;
        else this.account = fresh;
        this.transactions = fresh.wallet?.transactions ?? [];
        this.loadStats();
        this.loadActivitiesInternal();
      },
    });
  }

  /** Load client wallets from API (call again after fixing backend / connection). */
  loadClientAccounts(): void {
    const user = this.auth.currentUser();
    if (!user?.id) {
      this.loading = false;
      return;
    }
    this.loading = true;
    this.clientAccountsLoadError = null;
    this.accountSvc.getAccountsByOwner(+user.id).subscribe({
      next: (accounts) => {
        const list = accounts ?? [];
        const t = (a: Account) => String(a.type ?? '').toUpperCase();
        const savings = list.find((a) => t(a) === 'SAVINGS');
        const investment = list.find((a) => t(a) === 'INVESTMENT');
        this.account = savings ?? null;
        this.investmentAccount = investment ?? null;
        this.operationError = '';
        this.clientAccountsLoadError = null;

        if (!this.account && this.isClient) {
          this.createType = 'SAVINGS';
          this.submitCreate();
          this.loading = false;
          return;
        }

        if (!this.account && this.investmentAccount) {
          this.clientWalletView = 'INVESTMENT';
        } else if (!this.investmentAccount) {
          this.clientWalletView = 'SAVINGS';
        }
        if (this.clientWalletView === 'INVESTMENT' && !this.investmentAccount) {
          this.clientWalletView = 'SAVINGS';
        }

        const active = this.activeClientAccount;
        if (active) {
          this.transactions = active.wallet?.transactions ?? [];
          this.loadStats();
        }

        this.loading = false;
      },
      error: (err: HttpErrorResponse) => {
        this.loading = false;
        this.clientAccountsLoadError = this.httpErrorMessage(
          err,
          'Could not load your wallet accounts. If accounts already exist in the database, do not create duplicates — fix the API error and retry.',
        );
      },
    });
  }

  get balance(): number { return this.activeClientAccount?.wallet?.balance ?? 0; }

  openCreateModal(): void { this.createType = 'SAVINGS'; this.operationError = ''; this.showCreateModal = true; }
  closeCreateModal(): void { this.showCreateModal = false; }

  submitCreate(): void {
    const user = this.auth.currentUser();
    if (!user?.id) return;
    const holderName = user.username ?? `User #${user.id}`;
    this.accountSvc.createAccount(+user.id, this.createType, holderName).subscribe({
      next: (acc) => {
        if (acc.type === 'INVESTMENT') {
          this.investmentAccount = acc;
          this.clientWalletView = 'INVESTMENT';
        } else {
          this.account = acc;
          this.clientWalletView = 'SAVINGS';
        }
        this.transactions = acc.wallet?.transactions ?? [];
        this.showCreateModal = false;
        this.showInvestmentCreateModal = false;
        this.operationError = '';
        this.operationMessage = `${acc.type} account created successfully!`;
        this.loadStats();
      },
      error: (err: HttpErrorResponse) => {
        this.operationError = this.httpErrorMessage(err, 'Account creation failed. Please try again.');
      },
    });
  }

  openInvestmentCreate(): void {
    this.createType = 'INVESTMENT';
    this.operationError = '';
    this.showInvestmentCreateModal = true;
  }

  openDepositModal(): void { this.depositAmount = null; this.operationError = ''; this.showDepositModal = true; }
  closeDepositModal(): void { this.showDepositModal = false; }
  submitDeposit(): void {
    const acc = this.activeClientAccount;
    if (!acc || !this.depositAmount || this.depositAmount <= 0) return;
    const amount = this.depositAmount;
    this.accountSvc.deposit(acc.id, amount).subscribe({
      next: () => { this.operationMessage = `Deposited $${amount.toFixed(2)}`; this.showDepositModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Deposit failed.'; },
    });
  }

  openWithdrawModal(): void { this.withdrawAmount = null; this.operationError = ''; this.showWithdrawModal = true; }
  closeWithdrawModal(): void { this.showWithdrawModal = false; }
  submitWithdraw(): void {
    const acc = this.activeClientAccount;
    if (!acc || !this.withdrawAmount || this.withdrawAmount <= 0) return;
    const amount = this.withdrawAmount;
    this.accountSvc.withdraw(acc.id, amount).subscribe({
      next: () => { this.operationMessage = `Withdrew $${amount.toFixed(2)}`; this.showWithdrawModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Insufficient balance or account is blocked.'; },
    });
  }

  openTransferModal(): void { this.transferToAccountId = null; this.transferAmount = null; this.operationError = ''; this.showTransferModal = true; }
  closeTransferModal(): void { this.showTransferModal = false; }
  submitTransfer(): void {
    const acc = this.activeClientAccount;
    if (!acc || !this.transferToAccountId || !this.transferAmount || this.transferAmount <= 0) return;
    const amount = this.transferAmount;
    this.accountSvc.transfer(acc.id, this.transferToAccountId, amount).subscribe({
      next: () => { this.operationMessage = `Transferred $${amount.toFixed(2)}`; this.showTransferModal = false; this.refreshClientAccount(); },
      error: () => { this.operationError = 'Transfer failed. Check the account ID and your balance.'; },
    });
  }

  openActivities(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.showActivitiesModal = true;
    this.loadingActivities = true;
    this.accountSvc.getActivities(acc.id).subscribe({
      next: (a) => { this.activities = a; this.loadingActivities = false; },
      error: () => { this.loadingActivities = false; },
    });
  }
  closeActivities(): void { this.showActivitiesModal = false; }

  loadForecast(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.loadingForecast = true; this.forecast = null;
    this.accountSvc.forecast(acc.id, 30).subscribe({
      next: (f) => { this.forecast = f; this.loadingForecast = false; },
      error: () => { this.loadingForecast = false; },
    });
  }

  loadAdvice(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.loadingAdvice = true; this.advice = null;
    this.accountSvc.getAccountTypeAdvice(acc.id).subscribe({
      next: (a) => { this.advice = a; this.loadingAdvice = false; },
      error: () => { this.loadingAdvice = false; },
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

  private httpErrorMessage(err: HttpErrorResponse, fallback: string): string {
    const body = err.error;
    if (typeof body === 'string' && body.trim()) return body;
    if (body && typeof body.message === 'string') return body.message;
    if (err.message) return err.message;
    return fallback;
  }

  private loadStats(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.accountSvc.getStatistics(acc.id).subscribe({ next: (s) => (this.stats = s) });
  }

  private refreshClientAccount(): void {
    const active = this.activeClientAccount;
    if (!active) return;
    this.accountSvc.getAccount(active.id).subscribe({
      next: (acc) => {
        if (acc.type === 'INVESTMENT') this.investmentAccount = acc;
        else this.account = acc;
        this.transactions = acc.wallet?.transactions ?? [];
        this.loadStats();
      },
    });
  }

  private loadActivitiesInternal(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.loadingActivities = true;
    this.accountSvc.getActivities(acc.id).subscribe({
      next: (a) => {
        this.activities = a;
        this.loadingActivities = false;
      },
      error: () => {
        this.loadingActivities = false;
      },
    });
  }
}
