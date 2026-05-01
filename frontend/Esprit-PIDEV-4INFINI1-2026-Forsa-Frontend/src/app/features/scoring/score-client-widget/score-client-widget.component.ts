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
  showBoosterHub = signal(false);
  ocrLoading = signal<'STEG' | 'SONEDE' | null>(null);
  ocrMessage = signal<{ type: 'success' | 'error'; text: string } | null>(null);

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

  loadScore(): void {
    const user = this.auth.currentUser();
    if (!user?.id) { this.loading.set(false); return; }
    this.loading.set(true);
    this.error.set(false);
    this.aiScore.getCurrentScore(user.id).subscribe({
      next: (s) => { this.score.set(s); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); },
    });
  }

  recalculate(): void {
    const user = this.auth.currentUser();
    if (!user?.id) return;
    this.loading.set(true);
    this.aiScore.recalculateScore(user.id).subscribe({
      next: (s) => { this.score.set(s); this.loading.set(false); },
      error: () => { this.error.set(true); this.loading.set(false); },
    });
  }

  triggerOcr(type: 'STEG' | 'SONEDE'): void {
    if (this.ocrLoading()) return;
    if (type === 'STEG') this.stegInputRef.nativeElement.click();
    else this.sonedeInputRef.nativeElement.click();
  }

  onFileSelected(event: Event, type: 'STEG' | 'SONEDE'): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    const user = this.auth.currentUser();
    if (!user?.id) return;

    this.ocrLoading.set(type);
    this.ocrMessage.set(null);

    this.aiScore.verifyDocument(file, type).subscribe({
      next: (result: OcrResult) => {
        if (result.verified && result.paid_on_time) {
          this.aiScore.activateBooster(user.id, type).subscribe({
            next: (s) => {
              this.score.set(s);
              this.ocrLoading.set(null);
              this.ocrMessage.set({
                type: 'success',
                text: `Booster ${type} activé ! Votre seuil a été mis à jour.`,
              });
            },
            error: () => {
              this.ocrLoading.set(null);
              this.ocrMessage.set({ type: 'error', text: 'Erreur lors de l\'activation du booster.' });
            },
          });
        } else {
          this.ocrLoading.set(null);
          this.ocrMessage.set({ type: 'error', text: `Document ${type} non valide ou facture non payée.` });
        }
        input.value = '';
      },
      error: () => {
        this.ocrLoading.set(null);
        this.ocrMessage.set({ type: 'error', text: 'Erreur lors de la vérification du document.' });
        input.value = '';
      },
    });
  }

  get thresholdPercent(): number {
    const v = this.score()?.availableThreshold;
    if (!v) return 0;
    return Math.min((v / 10000) * 100, 100);
  }

  get boosterHint(): string | null {
    const s = this.score();
    if (!s || s.hasActiveCredit) return null;
    if (!s.sonedeBoosterActive) return 'Scan your SONEDE bill to increase your credit threshold';
    if (!s.stegBoosterActive) return 'Scan your STEG bill to increase your credit threshold';
    return null;
  }

  formatExpiry(dateStr: string | null | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: '2-digit' });
  }
}
