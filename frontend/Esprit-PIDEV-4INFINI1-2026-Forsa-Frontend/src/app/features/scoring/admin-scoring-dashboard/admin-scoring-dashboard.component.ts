import { DatePipe, DecimalPipe, NgClass } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import type { AIScoreDto, AIScoreLevel } from '../../../core/models/forsa.models';
import { AiScoreService } from '../services/ai-score.service';

interface ClientRow extends AIScoreDto {
  refreshing: boolean;
}

@Component({
  selector: 'app-admin-scoring-dashboard',
  standalone: true,
  imports: [DatePipe, DecimalPipe, NgClass],
  templateUrl: './admin-scoring-dashboard.component.html',
  styleUrl: './admin-scoring-dashboard.component.css',
})
export class AdminScoringDashboardComponent implements OnInit, OnDestroy {
  clients = signal<ClientRow[]>([]);
  loading = signal(true);
  lastRefresh = signal<Date | null>(null);

  private pollTimer: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly aiScore: AiScoreService) {}

  ngOnInit(): void {
    this.load();
    this.pollTimer = setInterval(() => this.load(), 30_000);
  }

  ngOnDestroy(): void {
    if (this.pollTimer) clearInterval(this.pollTimer);
  }

  load(): void {
    this.aiScore.getAllSummaries().subscribe({
      next: (list) => {
        this.clients.set(list.map(c => ({ ...c, refreshing: false })));
        this.loading.set(false);
        this.lastRefresh.set(new Date());
      },
      error: () => this.loading.set(false),
    });
  }

  recalculate(row: ClientRow): void {
    this.clients.update(list =>
      list.map(c => c.clientId === row.clientId ? { ...c, refreshing: true } : c)
    );
    this.aiScore.recalculateScore(Number(row.clientId)).subscribe({
      next: (updated) => {
        this.clients.update(list =>
          list.map(c => c.clientId === updated.clientId ? { ...updated, refreshing: false } : c)
        );
      },
      error: () => {
        this.clients.update(list =>
          list.map(c => c.clientId === row.clientId ? { ...c, refreshing: false } : c)
        );
      },
    });
  }

  levelClass(level: AIScoreLevel): string {
    switch (level) {
      case 'PREMIUM':   return 'badge--premium';
      case 'EXCELLENT': return 'badge--excellent';
      case 'VERY_GOOD': return 'badge--very-good';
      case 'GOOD':      return 'badge--good';
      case 'MEDIUM':    return 'badge--medium';
      case 'LOW':       return 'badge--low';
      case 'VERY_LOW':  return 'badge--very-low';
    }
  }

  scoreBar(score: number): number {
    return Math.round((score / 1000) * 100);
  }

  formatDate(iso: string | null | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('fr-FR', {
      day: '2-digit', month: '2-digit', year: '2-digit',
      hour: '2-digit', minute: '2-digit',
    });
  }
}
