import { DatePipe, DecimalPipe, NgClass } from '@angular/common';
import { Component, OnDestroy, OnInit, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
  imports: [DatePipe, DecimalPipe, NgClass, FormsModule, ForsaCardComponent],
  templateUrl: './admin-scoring-dashboard.component.html',
  styleUrl: './admin-scoring-dashboard.component.css',
})
export class AdminScoringDashboardComponent implements OnInit, OnDestroy {
  clients    = signal<ClientRow[]>([]);
  loading    = signal(true);
  lastRefresh = signal<Date | null>(null);
  search     = signal('');
  sortField  = signal<'score' | 'threshold' | 'name'>('score');
  sortDesc   = signal(true);

  readonly filtered = computed(() => {
    const q   = this.search().trim().toLowerCase();
    const all = this.clients();
    const out = q
      ? all.filter(c =>
          (c.clientName  ?? '').toLowerCase().includes(q) ||
          (c.clientEmail ?? '').toLowerCase().includes(q) ||
          String(c.clientId).includes(q))
      : all;

    const field = this.sortField();
    const desc  = this.sortDesc();
    return [...out].sort((a, b) => {
      let va: number, vb: number;
      if (field === 'score')     { va = a.score ?? 0;           vb = b.score ?? 0; }
      else if (field === 'threshold') { va = a.creditThreshold ?? 0; vb = b.creditThreshold ?? 0; }
      else                       { va = 0; vb = 0; } // name handled via string below
      if (field === 'name') {
        const sa = (a.clientName ?? '').toLowerCase();
        const sb = (b.clientName ?? '').toLowerCase();
        return desc ? sb.localeCompare(sa) : sa.localeCompare(sb);
      }
      return desc ? vb - va : va - vb;
    });
  });

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

  toggleSort(field: 'score' | 'threshold' | 'name'): void {
    if (this.sortField() === field) {
      this.sortDesc.update(d => !d);
    } else {
      this.sortField.set(field);
      this.sortDesc.set(true);
    }
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
                  refreshing:        false,
                  score:             updated.score,
                  scoreLevel:        String(updated.scoreLevel),
                  creditThreshold:   updated.creditThreshold,
                  hasActiveCredit:   updated.hasActiveCredit,
                  lastCalculatedAt:  updated.lastCalculatedAt ?? null,
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
    switch (level as AIScoreLevel) {
      case 'PREMIUM':   return 'badge--premium';
      case 'EXCELLENT': return 'badge--excellent';
      case 'VERY_GOOD': return 'badge--very-good';
      case 'GOOD':      return 'badge--good';
      case 'MEDIUM':    return 'badge--medium';
      case 'LOW':       return 'badge--low';
      case 'VERY_LOW':  return 'badge--very-low';
      default:          return 'badge--medium';
    }
  }

  levelLabel(level: string): string {
    const map: Record<string, string> = {
      PREMIUM: 'Premium', EXCELLENT: 'Excellent', VERY_GOOD: 'Very Good',
      GOOD: 'Good', MEDIUM: 'Medium', LOW: 'Low', VERY_LOW: 'Very Low',
    };
    return map[level] ?? level.replace(/_/g, ' ');
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

  initials(row: ClientRow): string {
    const name = row.clientName ?? `C${row.clientId}`;
    return name.trim().charAt(0).toUpperCase();
  }
}
