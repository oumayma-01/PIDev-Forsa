import { Component, inject, signal, effect, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit, computed } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { PremiumPaymentService } from '../shared/services/premium-payment.service';
import { InsuranceAnalyticsService } from '../shared/services/insurance-analytics.service';
import { InsurancePolicyService } from '../shared/services/insurance-policy.service';
import { PaymentStatus, PolicyStatus } from '../shared/enums/insurance.enums';
import { InsurancePolicy, PremiumPayment } from '../shared/models/insurance.models';
import { InsuranceOverviewDTO } from '../shared/models/insurance-analytics.models';
import { CommonModule } from '@angular/common';
import { Chart } from 'chart.js/auto';
import { interval, Subscription } from 'rxjs';
import { startWith, switchMap, catchError } from 'rxjs/operators';
import { GlobalNotificationService } from '../../../core/services/global-notification.service';

@Component({
  selector: 'app-insurance-hub',
  standalone: true,
  imports: [RouterLink, ForsaCardComponent, ForsaIconComponent, ForsaBadgeComponent, ForsaButtonComponent, CommonModule],
  templateUrl: './insurance-hub.component.html',
  styleUrl: './insurance-hub.component.css',
})
export class InsuranceHubComponent implements OnInit, OnDestroy, AfterViewInit {
  readonly auth = inject(AuthService);
  private readonly paymentService = inject(PremiumPaymentService);
  private readonly analyticsService = inject(InsuranceAnalyticsService);
  private readonly policyService = inject(InsurancePolicyService);
  private readonly globalNotifService = inject(GlobalNotificationService);

  readonly PaymentStatus = PaymentStatus;
  readonly PolicyStatus = PolicyStatus;
  upcomingReminder = signal<PremiumPayment | null>(null);
  suspendedPolicy = signal<InsurancePolicy | null>(null);

  // Analytics State
  overview = signal<InsuranceOverviewDTO | null>(null);
  isLoadingAnalytics = signal<boolean>(false);
  analyticsError = signal<string | null>(null);
  lastUpdated = signal<Date | null>(null);
  
  @ViewChild('claimsTrendChart') claimsTrendCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('productRevenueChart') productRevenueCanvas!: ElementRef<HTMLCanvasElement>;
  
  private charts: Chart[] = [];
  private refreshSub?: Subscription;

  constructor() {
    effect(() => {
      const user = this.auth.currentUser();
      if (user) {
        console.log('Hub Effect: User identified', user.username, 'Roles:', user.roles);
        this.checkReminders();
        if (this.isAdminOrAgent) {
          console.log('Hub Effect: Admin/Agent access confirmed. Starting polling.');
          this.startAnalyticsPolling();
        } else {
          console.log('Hub Effect: User is not Admin/Agent');
        }
      } else {
        console.log('Hub Effect: No user session found');
      }
    });
  }

  ngOnInit() {
    console.log('Hub: OnInit. Triggering initial analytics fetch...');
    if (this.isAdminOrAgent) {
      this.startAnalyticsPolling();
    }
  }

  ngAfterViewInit() {
    // Charts will init after data load
  }

  ngOnDestroy() {
    this.refreshSub?.unsubscribe();
    this.charts.forEach(c => c.destroy());
  }

  startAnalyticsPolling() {
    if (this.refreshSub) {
       console.log('Hub: Polling already active');
       return;
    }

    console.log('Hub: Creating polling interval');
    this.refreshSub = interval(60000) // 1 minute polling for general analytics
      .pipe(
        startWith(0),
        switchMap(() => {
          console.log('Hub: Fetching analytics data...');
          if (!this.overview()) this.isLoadingAnalytics.set(true);
          return this.analyticsService.getOverview().pipe(
            catchError(err => {
              console.error('Hub: API error in polling', err);
              this.isLoadingAnalytics.set(false);
              this.analyticsError.set('Lost connection to analytics terminal. Retrying...');
              return []; // Return empty so it doesn't emit data but stays alive
            })
          );
        })
      )
      .subscribe({
        next: (data) => {
          console.log('Hub: Analytics data received', data);
          this.overview.set(data);
          this.isLoadingAnalytics.set(false);
          this.analyticsError.set(null);
          this.lastUpdated.set(new Date());
          console.log('Hub: Scheduling chart init...');
          setTimeout(() => {
            console.log('Hub: Running initCharts now');
            this.initCharts();
          }, 200);
        },
        error: (err) => {
          console.error('Hub: Failed to load insurance analytics', err);
          this.isLoadingAnalytics.set(false);
          this.analyticsError.set('Could not load analytics. Please try again later.');
        }
      });
  }

  private initCharts() {
    const data = this.overview();
    console.log('Hub: initCharts data state:', { 
      hasData: !!data, 
      claims: data?.claimsAnalytics?.monthlyTrends?.length,
      products: data?.popularProducts?.length 
    });

    if (!data || !data.claimsAnalytics || !data.popularProducts) {
      console.warn('Hub: Missing data for charts');
      return;
    }

    try {
      this.charts.forEach(c => c.destroy());
      this.charts = [];

      // 1. Claims Trend Chart
      if (this.claimsTrendCanvas && data.claimsAnalytics.monthlyTrends?.length > 0) {
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const c = new Chart(this.claimsTrendCanvas.nativeElement, {
          type: 'line',
          data: {
            labels: data.claimsAnalytics.monthlyTrends.map((t: any) => months[t.month - 1] || '???'),
            datasets: [{
              label: 'Claims Volume',
              data: data.claimsAnalytics.monthlyTrends.map((t: any) => t.count),
              borderColor: '#1e40af',
              backgroundColor: 'rgba(30, 64, 175, 0.1)',
              fill: true,
              tension: 0.4
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: { y: { beginAtZero: true }, x: { grid: { display: false } } }
          }
        });
        this.charts.push(c);
      }

      // 2. Product Revenue Chart
      if (this.productRevenueCanvas && data.popularProducts.length > 0) {
        const c = new Chart(this.productRevenueCanvas.nativeElement, {
          type: 'doughnut',
          data: {
            labels: data.popularProducts.map((p: any) => p.productName || 'Unknown'),
            datasets: [{
              data: data.popularProducts.map((p: any) => p.totalRevenue),
              backgroundColor: ['#1e40af', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
              borderWidth: 0
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { position: 'right' } }
          }
        });
        this.charts.push(c);
      }
    } catch (err) {
      console.error('Hub: Chart initialization failed', err);
    }
  }

  private checkReminders(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    if (!roles.includes('ROLE_CLIENT')) return;

    this.paymentService.getMyPayments().subscribe({
      next: (payments) => {
        const overdue = payments.find(p => p.status === PaymentStatus.OVERDUE);
        if (overdue) {
          this.upcomingReminder.set(overdue);
        } else {
          const soon = payments
            .filter(p => p.status === PaymentStatus.PENDING && !!p.dueDate)
            .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime())[0];
          
          if (soon && soon.dueDate) {
            const diffDays = Math.ceil((new Date(soon.dueDate).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24));
            if (diffDays <= 7) {
              this.upcomingReminder.set(soon);
              this.globalNotifService.addNotification({
                id: `ins-rem-${soon.id}`,
                message: `Upcoming Premium: ${soon.amount} TND due on ${soon.dueDate}`,
                type: 'insurance-warning',
                actionRoute: '/dashboard/insurance/client/my-payments'
              });
            }
          }
        }
      }
    });

    // Also check for suspended policies
    this.policyService.getMyPolicies().subscribe({
      next: (policies: InsurancePolicy[]) => {
        const suspended = policies.find((p: InsurancePolicy) => p.status === PolicyStatus.SUSPENDED);
        if (suspended) {
          this.suspendedPolicy.set(suspended);
          this.globalNotifService.addNotification({
            id: `ins-susp-${suspended.id}`,
            message: `CRITICAL: Policy #${suspended.policyNumber} is SUSPENDED. coverage is inactive.`,
            type: 'insurance-critical',
            actionRoute: '/dashboard/insurance/client/my-payments'
          });
        }
      }
    });
  }

  closeReminder(): void {
    this.upcomingReminder.set(null);
  }

  get isAdminOrAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_AGENT');
  }

  get isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_CLIENT');
  }

  modules = computed(() => {
    const data = this.overview();
    return [
      {
        title: 'Insurance Products',
        description: 'Catalogue of accessible health, life, and crop coverage.',
        icon: 'shield',
        route: 'products',
        tone: 'blue',
        stats: data ? `${data.totalProducts} Products` : 'Manage Catalogue',
      },
      {
        title: 'Policies',
        description: 'Review and approve active coverage across the platform.',
        icon: 'layout-dashboard',
        route: 'policies',
        tone: 'emerald',
        stats: data ? `${data.activePolicies} Active / ${data.suspendedPolicies} Suspended` : 'Policy Management',
      },
      {
        title: 'Insurance Claims',
        description: 'Process indemnification requests and track status.',
        icon: 'alert-circle',
        route: 'claims',
        tone: 'amber',
        stats: data ? `${data.claimsAnalytics.totalClaims} Total Claims` : 'Claim Processing',
      },
      {
        title: 'Premium Payments',
        description: 'Monitor periodic payments for all active policies.',
        icon: 'credit-card',
        route: 'payments',
        tone: 'rose',
        stats: 'Billing Terminal',
      },
    ];
  });
}
