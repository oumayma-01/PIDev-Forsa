import { DatePipe, DecimalPipe, NgClass } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { AccountService } from '../../../core/services/account.service';
import { AuthService } from '../../../core/services/auth.service';
import type { AIScoreDto } from '../../../core/models/forsa.models';
import type {
  Account,
  Activity,
  Transaction,
  WalletStatisticsDTO,
  WalletForecastDTO,
  AccountTypeAdviceDTO,
} from '../../../core/models/wallet.models';
import { AiScoreService } from '../../scoring/services/ai-score.service';

@Component({
  selector: 'app-wallet-overview',
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    NgClass,
    FormsModule,
    RouterLink,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
  ],
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
  stats: WalletStatisticsDTO | null = null;
  forecast: WalletForecastDTO | null = null;
  advice: AccountTypeAdviceDTO | null = null;
  /** Latest credit score DTO — loaded with Account Advice for personalised “raise score” tips. */
  creditAdviceScore: AIScoreDto | null = null;
  creditAdviceLines: string[] = [];

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
  /** Client: wallet transaction list — shown only after opening Activity Log. */
  showClientRecentActivity = false;
  loadingAdminOp = false;

  // ── Modals ────────────────────────────────────────────────────────────────────
  showCreateModal = false;
  showDepositModal = false;
  showWithdrawModal = false;
  showTransferModal = false;
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

  constructor(
    private readonly accountSvc: AccountService,
    private readonly auth: AuthService,
    private readonly aiScore: AiScoreService,
  ) {}

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
        this.accountSvc.getActivities(id).subscribe({
          next: (a) => (this.staffLookupActivities = this.sortActivitiesNewestFirst(a)),
        });
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

  /** Client “Recent activity” (transactions): newest first. */
  get transactionsNewestFirst(): Transaction[] {
    return [...(this.transactions ?? [])].sort((a, b) => {
      const ta = Date.parse(String(a.date ?? ''));
      const tb = Date.parse(String(b.date ?? ''));
      if (Number.isFinite(ta) && Number.isFinite(tb)) return tb - ta;
      return Number(b.id) - Number(a.id);
    });
  }

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

  openTransferModal(): void {
    const acc = this.activeClientAccount;
    if (!acc || String(acc.type).toUpperCase() !== 'INVESTMENT') {
      this.operationError = 'Transfers are only available on your investment account.';
      return;
    }
    this.transferToAccountId = null;
    this.transferAmount = null;
    this.operationError = '';
    this.showTransferModal = true;
  }
  closeTransferModal(): void { this.showTransferModal = false; }
  submitTransfer(): void {
    const acc = this.activeClientAccount;
    if (!acc || String(acc.type).toUpperCase() !== 'INVESTMENT') {
      this.operationError = 'Transfers are only available on your investment account.';
      return;
    }
    if (!this.transferToAccountId || !this.transferAmount || this.transferAmount <= 0) return;
    this.operationError = '';
    const amount = this.transferAmount;
    const toId = this.transferToAccountId;

    // Resolve recipient type before POST so savings (or bad IDs) never hit transfer, even if backend were misconfigured.
    this.accountSvc.getAccount(toId).subscribe({
      next: (target) => {
        const destType = String(target?.type ?? '').toUpperCase();
        if (destType !== 'INVESTMENT') {
          this.operationError =
            destType === 'SAVINGS'
              ? 'A savings account cannot receive transfers. Open the savings wallet and use Add funds, or enter an investment account ID.'
              : 'Only an investment account can receive this transfer. Check the recipient account ID.';
          return;
        }
        this.accountSvc.transfer(acc.id, toId, amount).subscribe({
          next: () => {
            this.operationMessage = `Transferred $${amount.toFixed(2)}`;
            this.showTransferModal = false;
            this.refreshClientAccount();
          },
          error: (err: HttpErrorResponse) => {
            const text = this.readTransferErrorMessage(err);
            this.operationError =
              text ||
              'Transfer could not be completed. You can only send to another investment account; savings accounts must be funded with Add funds.';
          },
        });
      },
      error: () => {
        this.operationError =
          'Could not load the recipient account. Check the account ID; only investment accounts can receive transfers.';
      },
    });
  }

  /** Spring returns `{ message: "ERROR_TRANSFER: …" }` on 400; surface that text in the modal. */
  private readTransferErrorMessage(err: HttpErrorResponse): string {
    const body = err.error;
    let raw = '';
    if (body && typeof body === 'object' && 'message' in body) {
      raw = String((body as { message?: unknown }).message ?? '').trim();
    } else if (typeof body === 'string') {
      const s = body.trim();
      if (s.startsWith('{')) {
        try {
          const o = JSON.parse(s) as { message?: string };
          raw = (o.message ?? '').trim();
        } catch {
          raw = s;
        }
      } else {
        raw = s;
      }
    }
    if (!raw) {
      return err.status === 0 ? 'Network error. Check your connection and try again.' : '';
    }
    const prefix = 'ERROR_TRANSFER:';
    if (raw.toUpperCase().startsWith(prefix)) {
      return raw.slice(prefix.length).trim();
    }
    return raw;
  }

  openActivities(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.showClientRecentActivity = true;
    this.refreshClientAccount();
  }

  dismissClientRecentActivity(): void {
    this.showClientRecentActivity = false;
  }

  loadForecast(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.loadingForecast = true; this.forecast = null;
    this.accountSvc.forecast(acc.id, 30).subscribe({
      next: (f) => { this.forecast = f; this.loadingForecast = false; },
      error: () => { this.loadingForecast = false; },
    });
  }

  dismissForecast(): void {
    this.forecast = null;
  }

  dismissAdvice(): void {
    this.advice = null;
    this.creditAdviceScore = null;
    this.creditAdviceLines = [];
  }

  loadAdvice(): void {
    const acc = this.activeClientAccount;
    if (!acc) return;
    this.loadingAdvice = true;
    this.advice = null;
    this.creditAdviceScore = null;
    this.creditAdviceLines = [];
    this.accountSvc.getAccountTypeAdvice(acc.id).subscribe({
      next: (a) => {
        this.advice = a;
        const cid = this.walletClientId();
        if (this.isClient && cid != null) {
          this.aiScore.getCurrentScore(cid).subscribe({
            next: (score) => {
              this.creditAdviceScore = score;
              this.creditAdviceLines = this.buildCreditAdviceLines(score);
              this.loadingAdvice = false;
            },
            error: () => {
              this.creditAdviceScore = null;
              this.creditAdviceLines = this.fallbackCreditAdviceLines();
              this.loadingAdvice = false;
            },
          });
        } else {
          this.loadingAdvice = false;
        }
      },
      error: () => {
        this.loadingAdvice = false;
        this.creditAdviceScore = null;
        this.creditAdviceLines = [];
      },
    });
  }

  /** Standing label from score loaded with Account Advice. */
  accountAdviceCreditLabel(): string {
    const s = this.creditAdviceScore;
    const raw = String(s?.scoreLevel ?? 'VERY_LOW').toUpperCase();
    const map: Record<string, string> = {
      VERY_LOW: 'Very low',
      LOW: 'Low',
      MEDIUM: 'Medium',
      GOOD: 'Good',
      VERY_GOOD: 'Very good',
      EXCELLENT: 'Excellent',
      PREMIUM: 'Premium',
    };
    return map[raw] ?? raw.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase());
  }

  accountAdviceCreditNumeric(): number {
    const s = this.creditAdviceScore;
    if (!s) return 0;
    const v = Number(s.currentScore ?? s.score ?? 0);
    return Number.isFinite(v) ? Math.round(Math.min(1000, Math.max(0, v))) : 0;
  }

  accountAdviceCreditToneClass(): string {
    const s = this.creditAdviceScore;
    const lvl = String(s?.scoreLevel ?? '').toUpperCase();
    if (['VERY_LOW', 'LOW'].includes(lvl)) return 'standing-strip standing-strip--risk';
    if (lvl === 'MEDIUM') return 'standing-strip standing-strip--mid';
    return 'standing-strip standing-strip--ok';
  }

  private walletClientId(): number | null {
    const id = this.auth.currentUser()?.id as string | number | undefined;
    if (id == null) return null;
    if (typeof id === 'string' && id.trim() === '') return null;
    return typeof id === 'number' ? id : Number(id);
  }

  private buildCreditAdviceLines(s: AIScoreDto): string[] {
    const lines: string[] = [];
    const thr = Number(s.availableThreshold ?? 0);
    const pts = Number(s.currentScore ?? s.score ?? 0);
    const ptsRounded = Number.isFinite(pts) ? Math.round(Math.min(1000, Math.max(0, pts))) : 0;

    if (s.hasActiveCredit) {
      lines.push(
        'You already have an active loan. Repay it on time first; then your borrowing limit can be calculated again.',
      );
      return lines;
    }

    if (thr <= 0) {
      lines.push(
        'Right now you are not offered a new borrowing amount (0 TND). That usually means your profile needs to be stronger before Forsa can extend credit.',
      );
      if (ptsRounded < 400) {
        lines.push(
          'Keep using your Digital Wallet (regular incoming funds help). When your profile crosses the eligibility band, a limit in TND can appear.',
        );
      }
      if (!s.stegBoosterActive || !s.sonedeBoosterActive) {
        lines.push(
          'Open My score (link below) and upload STEG and SONEDE bills when they are paid on time — each verified bill can raise how much you may borrow.',
        );
      }
    } else {
      lines.push(
        `You are eligible to borrow up to about ${Math.round(thr).toLocaleString('fr-FR')} TND, based on your current profile.`,
      );
      lines.push('Pay utilities on time and keep wallet activity steady to move toward a higher limit.');
      if (!s.stegBoosterActive || !s.sonedeBoosterActive) {
        lines.push('You can still upload STEG / SONEDE bills from My score for an extra boost if they verify successfully.');
      }
    }
    return lines;
  }

  private fallbackCreditAdviceLines(): string[] {
    return [
      'Keep using your Digital Wallet with regular incoming funds.',
      'Open My score to upload STEG or SONEDE bills (clear photo, paid on time) so your borrowing limit can be recalculated.',
    ];
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

  /** Activity log rows: newest first (matches API order; safe if older backend). */
  private sortActivitiesNewestFirst(list: Activity[] | null | undefined): Activity[] {
    return [...(list ?? [])].sort((a, b) => {
      const ta = Date.parse(String(a.timestamp ?? ''));
      const tb = Date.parse(String(b.timestamp ?? ''));
      if (Number.isFinite(ta) && Number.isFinite(tb)) return tb - ta;
      return String(b.timestamp ?? '').localeCompare(String(a.timestamp ?? ''));
    });
  }
}
