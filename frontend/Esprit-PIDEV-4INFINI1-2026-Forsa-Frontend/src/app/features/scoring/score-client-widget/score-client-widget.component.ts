import { DecimalPipe, DatePipe, NgClass } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { AiScoreService, OcrResult } from '../services/ai-score.service';
import type { AIScoreDto } from '../../../core/models/forsa.models';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

@Component({
  selector: 'app-score-client-widget',
  standalone: true,
  imports: [DecimalPipe, DatePipe, NgClass, ForsaCardComponent, ForsaButtonComponent, ForsaIconComponent],
  templateUrl: './score-client-widget.component.html',
  styleUrl: './score-client-widget.component.css',
})
export class ScoreClientWidgetComponent implements OnInit, OnDestroy {
  @ViewChild('stegInput') stegInputRef!: ElementRef<HTMLInputElement>;
  @ViewChild('sonedeInput') sonedeInputRef!: ElementRef<HTMLInputElement>;

  score = signal<AIScoreDto | null>(null);
  loading = signal(true);
  error = signal(false);
  docLoading = signal<'STEG' | 'SONEDE' | null>(null);
  billMessage = signal<{ type: 'success' | 'error'; text: string } | null>(null);

  private refreshTimer: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly auth: AuthService, private readonly aiScore: AiScoreService) {}

  async ngOnInit(): Promise<void> {
    await this.auth.ensureSessionFromApi();
    this.loadScore();
    this.refreshTimer = setInterval(() => this.loadScore(), 60_000);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) clearInterval(this.refreshTimer);
  }

  private clientId(): number | null {
    const id = this.auth.currentUser()?.id as string | number | undefined;
    if (id == null) return null;
    if (typeof id === 'string' && id.trim() === '') return null;
    return typeof id === 'number' ? id : Number(id);
  }

  loadScore(): void {
    const cid = this.clientId();
    if (cid == null) {
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.error.set(false);
    this.aiScore.getCurrentScore(cid).subscribe({
      next: (s) => {
        this.score.set(s);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  triggerBillUpload(type: 'STEG' | 'SONEDE'): void {
    if (this.docLoading()) return;
    if (type === 'STEG') this.stegInputRef.nativeElement.click();
    else this.sonedeInputRef.nativeElement.click();
  }

  onFileSelected(event: Event, type: 'STEG' | 'SONEDE'): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const cid = this.clientId();
    if (cid == null) return;

    this.docLoading.set(type);
    this.billMessage.set(null);

    this.aiScore.verifyDocument(file, type).subscribe({
      next: (result: OcrResult) => {
        const paidOnTime =
          result.paid_on_time === true ||
          (result as unknown as { paidOnTime?: boolean }).paidOnTime === true;
        if (result.verified && paidOnTime) {
          this.aiScore.activateBooster(cid, type).subscribe({
            next: (s) => {
              this.score.set(s);
              this.docLoading.set(null);
              this.billMessage.set({
                type: 'success',
                text: 'Your bill was verified! Your credit limit has been updated.',
              });
            },
            error: () => {
              this.docLoading.set(null);
              this.billMessage.set({
                type: 'error',
                text: 'Could not verify your bill. Please upload a clear photo.',
              });
            },
          });
        } else {
          this.docLoading.set(null);
          this.billMessage.set({
            type: 'error',
            text: 'Could not verify your bill. Please upload a clear photo.',
          });
        }
        input.value = '';
      },
      error: () => {
        this.docLoading.set(null);
        this.billMessage.set({
          type: 'error',
          text: 'Could not verify your bill. Please upload a clear photo.',
        });
        input.value = '';
      },
    });
  }

  get thresholdPercent(): number {
    const v = this.score()?.availableThreshold;
    if (v == null || v <= 0) return 0;
    return Math.min((v / 10000) * 100, 100);
  }

  formatExpiry(dateStr: string | null | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  /** Points 0–1000 from API (each client differs). */
  clientNumericScore(): number {
    const s = this.score();
    if (!s) return 0;
    const v = Number(s.currentScore ?? s.score ?? 0);
    return Number.isFinite(v) ? Math.round(Math.min(1000, Math.max(0, v))) : 0;
  }

  /** Human label for AIScoreDto.scoreLevel — personalised per client. */
  clientStandingLabel(): string {
    const s = this.score();
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

  /** Visual band for standing strip. */
  clientStandingToneClass(): string {
    const s = this.score();
    const lvl = String(s?.scoreLevel ?? '').toUpperCase();
    if (['VERY_LOW', 'LOW'].includes(lvl)) return 'standing-strip standing-strip--risk';
    if (lvl === 'MEDIUM') return 'standing-strip standing-strip--mid';
    return 'standing-strip standing-strip--ok';
  }
}
