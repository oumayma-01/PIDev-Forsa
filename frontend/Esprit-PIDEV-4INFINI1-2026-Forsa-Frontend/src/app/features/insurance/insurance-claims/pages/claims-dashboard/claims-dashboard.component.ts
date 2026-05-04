import { Component, OnInit, inject, signal, ViewChild, ElementRef, AfterViewInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Chart } from 'chart.js/auto';
import { interval, Subscription } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';
import { ClaimsDashboardService } from '../../../shared/services/claims-dashboard.service';
import { ClaimsDashboardDTO } from '../../../shared/models/claims-dashboard.models';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { AuthService } from '../../../../../core/services/auth.service';

@Component({
  selector: 'app-claims-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaBadgeComponent,
    ForsaButtonComponent
  ],
  templateUrl: './claims-dashboard.component.html',
  styleUrl: './claims-dashboard.component.css'
})
export class ClaimsDashboardComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly dashboardService = inject(ClaimsDashboardService);
  private readonly authService = inject(AuthService);

  // New signal for showing refresh toast
  showRefreshMessage = signal<boolean>(false);

  @ViewChild('statusChart') statusChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('trendChart') trendChartCanvas!: ElementRef<HTMLCanvasElement>;
  @ViewChild('typeChart') typeChartCanvas!: ElementRef<HTMLCanvasElement>;

  data = signal<ClaimsDashboardDTO | null>(null);
  loading = signal<boolean>(true);
  error = signal<string | null>(null);
  lastUpdated = signal<Date>(new Date());

  private charts: any[] = [];
  private refreshSubscription?: Subscription;

  ngOnInit() {
    this.startAutoRefresh();
  }

  ngOnDestroy() {
    this.refreshSubscription?.unsubscribe();
  }

  ngAfterViewInit() {
    // Charts will be initialized after data is loaded
  }

  startAutoRefresh() {
    // Poll every 30 seconds
    this.refreshSubscription = interval(30000)
      .pipe(
        startWith(0),
        switchMap(() => {
          if (!this.data()) this.loading.set(true);
          return this.dashboardService.getAnalytics();
        })
      )
      .subscribe({
        next: (resp) => {
      this.data.set(resp);
      this.loading.set(false);
      this.lastUpdated.set(new Date());
      // Show toast for auto-refresh
      this.showRefreshMessage.set(true);
      setTimeout(() => this.showRefreshMessage.set(false), 2000);
      setTimeout(() => this.initCharts(), 0);
        },
        error: (err) => {
          this.error.set('Failed to load dashboard analytics');
          this.loading.set(false);
        }
      });
  }

  loadData() {
    // Manual trigger
    this.loading.set(true);
    this.dashboardService.getAnalytics().subscribe({
      next: (resp) => {
        this.data.set(resp);
        this.loading.set(false);
        this.lastUpdated.set(new Date());
        this.showRefreshMessage.set(true);
        setTimeout(() => this.showRefreshMessage.set(false), 2000);
        setTimeout(() => this.initCharts(), 0);
      },
      error: (err) => {
        this.error.set('Failed to load dashboard analytics');
        this.loading.set(false);
      }
    });
  }

  canEdit(): boolean {
    const user = this.authService.currentUser();
    if (!user) return false;
    return user.roles.some(r => r === 'ROLE_ADMIN' || r === 'ROLE_AGENT');
  }

  initCharts() {
    const d = this.data();
    if (!d) return;

    // Destroy existing charts if any
    this.charts.forEach(c => c.destroy());
    this.charts = [];

    // 1. Status Chart (Doughnut)
    if (this.statusChartCanvas) {
      const statusChart = new Chart(this.statusChartCanvas.nativeElement, {
        type: 'doughnut',
        data: {
          labels: d.claimsByStatus.map(s => s.status),
          datasets: [{
            data: d.claimsByStatus.map(s => s.count),
            backgroundColor: ['#10b981', '#f59e0b', '#ef4444', '#6366f1', '#94a3b8'],
            borderWidth: 0,
            hoverOffset: 10
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'bottom', labels: { boxWidth: 12, padding: 15 } }
          }
        }
      });
      this.charts.push(statusChart);
    }

    // 2. Trend Chart (Line)
    if (this.trendChartCanvas) {
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      const trendChart = new Chart(this.trendChartCanvas.nativeElement, {
        type: 'line',
        data: {
          labels: d.monthlyTrends.map(t => months[t.month - 1] + ' ' + t.year),
          datasets: [{
            label: 'Claims Count',
            data: d.monthlyTrends.map(t => t.count),
            borderColor: '#6366f1',
            backgroundColor: 'rgba(99, 102, 241, 0.1)',
            fill: true,
            tension: 0.4,
            pointRadius: 4,
            pointHoverRadius: 6
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
            x: { grid: { display: false } }
          }
        }
      });
      this.charts.push(trendChart);
    }

    // 3. Type Chart (Bar)
    if (this.typeChartCanvas) {
      const typeChart = new Chart(this.typeChartCanvas.nativeElement, {
        type: 'bar',
        data: {
          labels: d.claimsByType.map(t => t.policyType),
          datasets: [{
            label: 'Total Amount (TND)',
            data: d.claimsByType.map(t => t.totalAmount),
            backgroundColor: '#10b981',
            borderRadius: 6
          }]
        },
        options: {
          indexAxis: 'y',
          responsive: true,
          maintainAspectRatio: false,
          plugins: { legend: { display: false } },
          scales: {
            x: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
            y: { grid: { display: false } }
          }
        }
      });
      this.charts.push(typeChart);
    }
  }

  statusTone(status: string): any {
    const s = status.toUpperCase();
    if (s.includes('APPROVED')) return 'success';
    if (s.includes('REJECTED')) return 'danger';
    if (s.includes('PENDING')) return 'warning';
    return 'info';
  }
}
