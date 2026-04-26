import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { FeedbackService } from '../../../core/data/feedback.service';
import { FeedbackFacadeService } from '../feedback-facade.service';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';

type SatisfactionAvg = { group: string; avgRating: number };
type TrendItem = { period: string; count: number; label: string };

@Component({
  selector: 'app-feedback-stats',
  standalone: true,
  imports: [CommonModule, ForsaButtonComponent, ForsaCardComponent],
  templateUrl: './feedback-stats.component.html',
  styleUrl: './feedback-stats.component.css',
})
export class FeedbackStatsComponent implements OnInit {
  private readonly complaintService = inject(ComplaintService);
  private readonly feedbackService = inject(FeedbackService);
  private readonly feedbackFacade = inject(FeedbackFacadeService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  summary: any = null;
  byCategory: Record<string, number> = {};
  byPriority: Record<string, number> = {};
  trendsData: TrendItem[] = [];
  feedbackSummary: any = null;
  satisfactionAverages: SatisfactionAvg[] = [];
  responseSummary: any = null;
  feedbackTrendsData: TrendItem[] = [];
  loading = false;
  error = '';

  ngOnInit(): void {
    if (!this.isAdminOrAgent) {
      this.router.navigate(['/dashboard/feedback']);
      return;
    }
    this.load();
  }

  get isAdminOrAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_AGENT');
  }

  load(): void {
    this.loading = true;
    this.error = '';
    this.complaintService.getSummaryReport().subscribe({ next: (d) => (this.summary = d), error: () => (this.error = 'Unable to load complaint summary.') });
    this.complaintService.getStatsByCategory().subscribe({ next: (d) => (this.byCategory = d ?? {}), error: () => void 0 });
    this.complaintService.getStatsByPriority().subscribe({ next: (d) => (this.byPriority = d ?? {}), error: () => void 0 });
    const trendsRequest = (this.complaintService as any).getTrends
      ? (this.complaintService as any).getTrends(6)
      : this.complaintService.getTrendsLastMonths(6);
    trendsRequest.subscribe({
      next: (d: any) => {
        this.trendsData = (Array.isArray(d) ? d : []).map((item: any) => ({
          period: item['period'] ?? item.period ?? '',
          count: Number(item['count'] ?? item.count ?? 0),
          label: this.formatPeriodLabel(item['period'] ?? item.period ?? ''),
        }));
      },
      error: () => {
        this.trendsData = [];
      },
    });
    this.feedbackService.getSummary().subscribe({ next: (d) => (this.feedbackSummary = d), error: () => void 0 });
    this.feedbackFacade.getResponsesSummaryReport().subscribe({
      next: (d) => {
        const backendTotal = Number(d?.total ?? 0);
        this.feedbackFacade.getAllResponses().subscribe({
          next: (responses: any[]) => {
            const byStatus: Record<string, number> = {};
            (Array.isArray(responses) ? responses : []).forEach((r: any) => {
              const key = String(r?.responseStatus ?? 'PENDING').toUpperCase();
              byStatus[key] = (byStatus[key] ?? 0) + 1;
            });
            const computedTotal = Object.values(byStatus).reduce((acc, value) => acc + Number(value ?? 0), 0);
            this.responseSummary = {
              total: backendTotal > 0 ? backendTotal : computedTotal,
              byStatus,
            };
          },
          error: () => {
            this.responseSummary = {
              total: backendTotal,
              byStatus: {},
            };
          },
        });
      },
      error: () => {
        this.responseSummary = null;
      },
    });
    this.feedbackFacade.getFeedbackTrends(6).subscribe({
      next: (d: any[]) => {
        this.feedbackTrendsData = (Array.isArray(d) ? d : []).map((item: any) => ({
          period: item['period'] ?? item.period ?? '',
          count: Number(item['count'] ?? item.count ?? 0),
          label: this.formatPeriodLabel(item['period'] ?? item.period ?? ''),
        }));
      },
      error: () => {
        this.feedbackTrendsData = [];
      },
    });
    this.feedbackService.getAvgRatingByCategory().subscribe({
      next: (d: any) => {
        console.log('RAW avg rating response:', d);
        console.log('Type:', typeof d);
        console.log('Is array:', Array.isArray(d));
        this.satisfactionAverages = this.normalizeSatisfaction(d);
        this.loading = false;
      },
      error: () => {
        this.satisfactionAverages = [];
        this.loading = false;
      },
    });
  }

  total(): number {
    return Number(this.summary?.total ?? 0);
  }

  countStatus(status: string): number {
    return Number(this.summary?.byStatus?.[status] ?? 0);
  }

  categoryKeys(): string[] {
    return Object.keys(this.byCategory ?? {});
  }

  priorityKeys(): string[] {
    return Object.keys(this.byPriority ?? {});
  }

  getPriorityColor(priority: string): string {
    switch (priority) {
      case 'CRITICAL':
        return '#dc2626';
      case 'HIGH':
        return '#f97316';
      case 'MEDIUM':
        return '#f59e0b';
      case 'LOW':
        return '#22c55e';
      default:
        return '#94a3b8';
    }
  }

  getPriorityDonutBackground(): string {
    const keys = this.priorityKeys();
    const total = keys.reduce((acc, k) => acc + Number(this.byPriority[k] ?? 0), 0);
    if (!total) {
      return 'conic-gradient(#e2e8f0 0 100%)';
    }

    let start = 0;
    const parts: string[] = [];
    keys.forEach((key) => {
      const value = Number(this.byPriority[key] ?? 0);
      const slice = (value / total) * 100;
      const end = start + slice;
      parts.push(`${this.getPriorityColor(key)} ${start}% ${end}%`);
      start = end;
    });
    return `conic-gradient(${parts.join(', ')})`;
  }

  hasSatisfactionData(): boolean {
    return this.satisfactionAverages.some((x) => x.avgRating > 0);
  }

  ratingStars(value: number): string {
    const rounded = Math.max(0, Math.min(5, Math.round(value || 0)));
    return '★★★★★'.slice(0, rounded) + '☆☆☆☆☆'.slice(0, 5 - rounded);
  }

  satisfactionColor(group: string): string {
    if (group === 'VERY_SATISFIED') return '#16a34a';
    if (group === 'SATISFIED') return '#65a30d';
    if (group === 'NEUTRAL') return '#eab308';
    if (group === 'DISSATISFIED') return '#f97316';
    return '#dc2626';
  }

  satisfactionLevelsMissing(): boolean {
    return this.totalFeedbacks() > 0 && !this.hasSatisfactionData();
  }

  private normalizeSatisfaction(data: any): SatisfactionAvg[] {
    const defaults = ['VERY_SATISFIED', 'SATISFIED', 'NEUTRAL', 'DISSATISFIED', 'VERY_DISSATISFIED'];
    const map = new Map<string, number>();
    defaults.forEach((d) => map.set(d, 0));

    if (Array.isArray(data)) {
      data.forEach((item: any) => {
        const group = String(item?.group ?? item?.satisfactionLevel ?? item?.category ?? item?.label ?? 'UNKNOWN');
        const avgRating = Number(item?.avgRating ?? item?.averageRating ?? item?.rating ?? item?.value ?? 0);
        if (group) map.set(group, Number.isFinite(avgRating) ? avgRating : 0);
      });
    } else if (data && typeof data === 'object') {
      Object.entries(data).forEach(([k, v]) => map.set(k, Number(v ?? 0)));
    }

    return defaults.map((group) => ({ group, avgRating: map.get(group) ?? 0 }));
  }

  totalFeedbacks(): number {
    return Number(this.feedbackSummary?.total ?? this.feedbackSummary?.count ?? 0);
  }

  formatPeriodLabel(period: string): string {
    if (!period) return '';
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
      'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    const parts = period.split('-');
    if (parts.length === 2) {
      const monthIndex = parseInt(parts[1], 10) - 1;
      return `${months[monthIndex]} ${parts[0]}`;
    }
    return period;
  }

  get trendsSvgPath(): string {
    if (!this.trendsData.length) return '';
    const width = 600;
    const height = 200;
    const padding = 40;
    const maxCount = Math.max(...this.trendsData.map((d) => d.count), 1);
    const points = this.trendsData.map((d, i) => {
      const x = padding + (i / (this.trendsData.length - 1)) * (width - 2 * padding);
      const y = height - padding - (d.count / maxCount) * (height - 2 * padding);
      return { x, y, count: d.count, label: d.label };
    });
    return points.map((p, i) => (i === 0 ? 'M' : 'L') + p.x.toFixed(1) + ',' + p.y.toFixed(1)).join(' ');
  }

  get trendsPoints(): { x: number; y: number; count: number; label: string }[] {
    if (!this.trendsData.length) return [];
    const width = 600;
    const height = 200;
    const padding = 40;
    const maxCount = Math.max(...this.trendsData.map((d) => d.count), 1);
    return this.trendsData.map((d, i) => {
      const x = padding + (i / (this.trendsData.length - 1)) * (width - 2 * padding);
      const y = height - padding - (d.count / maxCount) * (height - 2 * padding);
      return { x, y, count: d.count, label: d.label };
    });
  }

  get feedbackTrendsSvgPath(): string {
    if (!this.feedbackTrendsData.length) return '';
    const width = 600;
    const height = 200;
    const padding = 40;
    const maxCount = Math.max(...this.feedbackTrendsData.map((d) => d.count), 1);
    const points = this.feedbackTrendsData.map((d, i) => {
      const x = padding + (i / (this.feedbackTrendsData.length - 1 || 1)) * (width - 2 * padding);
      const y = height - padding - (d.count / maxCount) * (height - 2 * padding);
      return { x, y, count: d.count, label: d.label };
    });
    return points.map((p, i) => (i === 0 ? 'M' : 'L') + p.x.toFixed(1) + ',' + p.y.toFixed(1)).join(' ');
  }

  get feedbackTrendsPoints(): { x: number; y: number; count: number; label: string }[] {
    if (!this.feedbackTrendsData.length) return [];
    const width = 600;
    const height = 200;
    const padding = 40;
    const maxCount = Math.max(...this.feedbackTrendsData.map((d) => d.count), 1);
    return this.feedbackTrendsData.map((d, i) => {
      const x = padding + (i / (this.feedbackTrendsData.length - 1 || 1)) * (width - 2 * padding);
      const y = height - padding - (d.count / maxCount) * (height - 2 * padding);
      return { x, y, count: d.count, label: d.label };
    });
  }

  responseCount(status: string): number {
    return Number(this.responseSummary?.byStatus?.[String(status).toUpperCase()] ?? 0);
  }
}
