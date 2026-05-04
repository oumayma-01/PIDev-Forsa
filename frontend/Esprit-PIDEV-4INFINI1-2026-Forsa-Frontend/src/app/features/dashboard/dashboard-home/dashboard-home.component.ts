import { Component, OnInit, AfterViewInit, ElementRef, computed, inject, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { UserAdminService } from '../../../core/services/user-admin.service';
import { UserDashboardOverview } from '../../../core/models/user-admin.model';
import { AdminFinanceService } from '../../../core/services/admin-finance.service';
import { AdminFinancialDashboard } from '../../../core/models/admin-finance.model';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { gsap } from 'gsap';
import { BaseChartDirective } from 'ng2-charts';
import type { ChartData, ChartOptions } from 'chart.js';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [ForsaCardComponent, ForsaIconComponent, RouterLink, CommonModule, BaseChartDirective],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.css',
})
export class DashboardHomeComponent implements OnInit, AfterViewInit {
  private readonly auth = inject(AuthService);
  private readonly userAdminService = inject(UserAdminService);
  private readonly adminFinanceService = inject(AdminFinanceService);
  private readonly el = inject(ElementRef);

  readonly stats = signal<UserDashboardOverview | null>(null);
  readonly adminFinance = signal<AdminFinancialDashboard | null>(null);

  readonly chartOptionsBase: any = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: { usePointStyle: true, boxWidth: 10 },
      },
    },
  };

  revenueChartData: ChartData<'line'> = { labels: [], datasets: [] };
  insuranceChartData: ChartData<'doughnut'> = { labels: [], datasets: [] };
  creditChartData: ChartData<'bar'> = { labels: [], datasets: [] };
  walletChartData: ChartData<'polarArea'> = { labels: [], datasets: [] };

  readonly revenueChartOptions: any = {
    ...this.chartOptionsBase,
    scales: {
      x: { grid: { display: false } },
      y: { beginAtZero: true },
    },
  };

  readonly insuranceChartOptions: any = {
    ...this.chartOptionsBase,
    cutout: '62%',
  };

  readonly creditChartOptions: any = {
    ...this.chartOptionsBase,
    indexAxis: 'y',
    plugins: { legend: { display: false } },
    scales: {
      x: { beginAtZero: true, grid: { color: 'rgba(148,163,184,0.2)' } },
      y: { grid: { display: false } },
    },
  };

  readonly walletChartOptions: any = {
    ...this.chartOptionsBase,
    scales: {
      r: { ticks: { display: false } },
    },
  };

  readonly userDistributionChartOptions: ChartOptions<'pie'> = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom',
        labels: { usePointStyle: true, boxWidth: 10, padding: 14 },
      },
    },
  };
  readonly quickActions = computed(() => {
    if (this.isAdmin) {
      return [
        { label: 'User management', description: 'Manage accounts and activation status.', path: '/dashboard/users', icon: 'users' as ForsaIconName },
        { label: 'Role access', description: 'Control sidebar permissions per role.', path: '/dashboard/roles', icon: 'shield-check' as ForsaIconName },
        { label: 'Feedback queue', description: 'Review complaints and response workflow.', path: '/dashboard/feedback', icon: 'message-square' as ForsaIconName },
        { label: 'Client AI scores', description: 'Real-time AI scores for all clients.', path: '/dashboard/scoring', icon: 'brain' as ForsaIconName },
      ];
    }
    return [
      { label: 'Credit operations', description: 'Review and follow active credit requests.', path: '/dashboard/credit', icon: 'credit-card' as ForsaIconName },
      { label: 'Insurance operations', description: 'Handle policies, products, and claims.', path: '/dashboard/insurance', icon: 'shield' as ForsaIconName },
      { label: 'Feedback queue', description: 'Treat complaints and keep customers informed.', path: '/dashboard/feedback', icon: 'message-square' as ForsaIconName },
    ];
  });

  get isAdmin(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN');
  }

  get isAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_AGENT');
  }

  get isAdminOrAgent(): boolean {
    return this.isAdmin || this.isAgent;
  }

  get dashboardHeading(): string {
    return this.isAdmin ? 'Platform command center' : 'Agent operations workspace';
  }

  get dashboardSubheading(): string {
    return this.isAdmin
      ? 'Track activation quality and governance in one place.'
      : 'Monitor customer activity, prioritize operations, and keep service levels high.';
  }

  get roleBadgeLabel(): string {
    return this.isAdmin ? 'Administrator view' : 'Agent view';
  }

  percentOf(part: number, total: number): number {
    if (total <= 0) {
      return 0;
    }
    return Math.round((part / total) * 100);
  }

  activationRatePercent(s: UserDashboardOverview): number {
    const raw = Number(s.activationRate ?? 0);
    if (Number.isFinite(raw) && raw > 0) {
      if (raw <= 1) {
        return Number((raw * 100).toFixed(1));
      }
      if (raw <= 100) {
        return Number(raw.toFixed(1));
      }
    }

    if (s.totalUsers <= 0) {
      return 0;
    }
    return Number(((s.activeUsers / s.totalUsers) * 100).toFixed(1));
  }

  userDistributionChartData(s: UserDashboardOverview): ChartData<'pie', number[], string> {
    return {
      labels: ['Clients', 'Agents', 'Administrators'],
      datasets: [
        {
          data: [s.totalClients, s.totalAgents, s.totalAdmins],
          backgroundColor: ['#2563eb', '#10b981', '#f59e0b'],
          borderWidth: 0,
        },
      ],
    };
  }

  ngOnInit(): void {
    if (this.isAdminOrAgent) {
      this.userAdminService.getDashboardOverview().subscribe({
        next: (res) => this.stats.set(res),
        error: () => console.error('Could not load dashboard stats'),
      });
    }

    if (this.isAdmin) {
      this.loadAdminFinancialAnalytics();
    }
  }

  ngAfterViewInit(): void {
    const root = this.el.nativeElement as HTMLElement;

    // Staggered card entrance animation
    const cards = root.querySelectorAll('app-forsa-card');
    if (cards.length) {
      gsap.from(Array.from(cards), {
        y: 24,
        opacity: 0,
        duration: 0.5,
        stagger: 0.1,
        ease: 'power3.out',
        delay: 0.2,
      });
    }

    // Animate heading
    const heading = root.querySelector('.page-head');
    if (heading) {
      gsap.from(heading, {
        y: -15,
        opacity: 0,
        duration: 0.4,
        ease: 'power2.out',
      });
    }

    // Charts are created after data arrives to avoid empty canvas initialization.
  }

  seedFinanceDemoData(): void {
    this.adminFinanceService.seedDemoData().subscribe({
      next: () => this.loadAdminFinancialAnalytics(),
      error: () => console.error('Could not seed demo data'),
    });
  }

  private loadAdminFinancialAnalytics(): void {
    this.adminFinanceService.getOverview().subscribe({
      next: (res) => {
        this.adminFinance.set(res);
        this.updateChartData(res);
      },
      error: () => console.error('Could not load admin financial analytics'),
    });
  }

  private updateChartData(data: AdminFinancialDashboard): void {
    this.revenueChartData = {
      labels: data.monthlyRevenueVsClaims.map((p) => p.month),
      datasets: [
        { label: 'Premium collected', data: data.monthlyRevenueVsClaims.map((p) => p.premiumCollected), borderColor: '#2563eb', backgroundColor: 'rgba(37,99,235,0.18)', tension: 0.35, fill: true, pointRadius: 2 },
        { label: 'Claims paid', data: data.monthlyRevenueVsClaims.map((p) => p.claimsPaid), borderColor: '#ef4444', backgroundColor: 'rgba(239,68,68,0.12)', tension: 0.35, fill: true, pointRadius: 2 },
      ],
    };

    this.insuranceChartData = {
      labels: data.insuranceStatusSplit.map((x) => x.label),
      datasets: [{ data: data.insuranceStatusSplit.map((x) => x.value), backgroundColor: ['#0ea5e9', '#22c55e', '#f59e0b', '#ef4444', '#8b5cf6', '#64748b'] }],
    };

    this.creditChartData = {
      labels: data.creditStatusSplit.map((x) => x.label),
      datasets: [{ label: 'Credit requests', data: data.creditStatusSplit.map((x) => x.value), backgroundColor: '#4f46e5', borderRadius: 8, barThickness: 14 }],
    };

    this.walletChartData = {
      labels: data.walletFlowSplit.map((x) => x.label),
      datasets: [{ data: data.walletFlowSplit.map((x) => x.value), backgroundColor: ['#22c55e', '#ef4444', '#0ea5e9', '#14b8a6', '#f59e0b'] }],
    };
  }
}
