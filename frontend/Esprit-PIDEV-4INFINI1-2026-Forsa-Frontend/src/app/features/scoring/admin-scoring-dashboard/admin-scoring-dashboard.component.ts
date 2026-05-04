import { DatePipe, DecimalPipe, NgClass } from '@angular/common';
import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import type { AIScoreDto, AIScoreLevel, AIScoreSummaryDto } from '../../../core/models/forsa.models';
import { ForsaDataTableComponent } from '../../../shared/ui/forsa-data-table/forsa-data-table.component';
import type {
  ForsaDataTablePageEvent,
  ForsaTableColumn,
} from '../../../shared/ui/forsa-data-table/forsa-data-table.types';
import { AiScoreService } from '../services/ai-score.service';

interface ClientRow extends AIScoreSummaryDto {
  refreshing: boolean;
}

@Component({
  selector: 'app-admin-scoring-dashboard',
  standalone: true,
  imports: [DatePipe, DecimalPipe, NgClass, ForsaCardComponent, ForsaDataTableComponent],
  templateUrl: './admin-scoring-dashboard.component.html',
  styleUrl: './admin-scoring-dashboard.component.css',
})
export class AdminScoringDashboardComponent implements OnInit, OnDestroy {
  clients = signal<ClientRow[]>([]);
  loading = signal(true);
  lastRefresh = signal<Date | null>(null);

  scoringPageIndex = 0;
  scoringPageSize = 10;
  readonly scoringTableColumns: ForsaTableColumn[] = [
    { key: 'client', label: 'Client' },
    { key: 'score', label: 'Score / 1000', width: '10rem' },
    { key: 'level', label: 'Level', width: '8rem' },
    { key: 'threshold', label: 'Credit threshold' },
    { key: 'loan', label: 'Active loan', align: 'center', width: '6rem' },
    { key: 'boosters', label: 'Boosters' },
    { key: 'date', label: 'Calculated at', width: '9rem' },
    { key: 'action', label: '', width: '3.25rem', align: 'right' },
  ];

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
        this.clients.set(list.map((c) => ({ ...c, refreshing: false })));
        this.scoringPageIndex = 0;
        this.loading.set(false);
        this.lastRefresh.set(new Date());
      },
      error: () => this.loading.set(false),
    });
  }

  get scoringClientsPaged(): ClientRow[] {
    const list = this.clients();
    const start = this.scoringPageIndex * this.scoringPageSize;
    return list.slice(start, start + this.scoringPageSize);
  }

  onScoringPage(ev: ForsaDataTablePageEvent): void {
    this.scoringPageIndex = ev.pageIndex;
    this.scoringPageSize = ev.pageSize;
  }

  recalculate(row: ClientRow): void {
    this.clients.update((list) =>
      list.map((c) => (c.clientId === row.clientId ? { ...c, refreshing: true } : c)),
    );
    this.aiScore.recalculateScore(Number(row.clientId)).subscribe({
      next: (updated: AIScoreDto) => {
        this.clients.update((list) =>
          list.map((c) =>
            c.clientId === updated.clientId
              ? {
                  ...c,
                  refreshing: false,
                  score: updated.score,
                  scoreLevel: String(updated.scoreLevel),
                  creditThreshold: updated.creditThreshold,
                  hasActiveCredit: updated.hasActiveCredit,
                  lastCalculatedAt: updated.lastCalculatedAt ?? null,
                  stegBoosterActive: updated.stegBoosterActive,
                  stegBoosterExpiry: updated.stegBoosterExpiry,
                  sonedeBoosterActive: updated.sonedeBoosterActive,
                  sonedeBoosterExpiry: updated.sonedeBoosterExpiry,
                }
              : c,
          ),
        );
      },
      error: () => {
        this.clients.update((list) =>
          list.map((c) => (c.clientId === row.clientId ? { ...c, refreshing: false } : c)),
        );
      },
    });
  }

  levelClass(level: string): string {
    const l = level as AIScoreLevel;
    switch (l) {
      case 'PREMIUM':
        return 'badge--premium';
      case 'EXCELLENT':
        return 'badge--excellent';
      case 'VERY_GOOD':
        return 'badge--very-good';
      case 'GOOD':
        return 'badge--good';
      case 'MEDIUM':
        return 'badge--medium';
      case 'LOW':
        return 'badge--low';
      case 'VERY_LOW':
        return 'badge--very-low';
      default:
        return 'badge--medium';
    }
  }

  formatLevelLabel(level: string): string {
    return level.replace(/_/g, ' ');
  }

  scoreBar(score: number): number {
    return Math.round((score / 1000) * 100);
  }

  formatDate(iso: string | null | undefined): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
