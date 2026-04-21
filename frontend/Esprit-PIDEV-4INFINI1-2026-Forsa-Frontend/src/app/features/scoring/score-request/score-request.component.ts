import { DecimalPipe, NgFor, NgIf, NgStyle, NgSwitch, NgSwitchCase } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import type { AIScoreDetailItem, AIScoreLevel, AIScoreResponse } from '../../../core/models/forsa.models';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { AiScoreService, type OcrResult } from '../services/ai-score.service';

/** Couleur CSS par niveau. */
const LEVEL_COLORS: Record<AIScoreLevel, string> = {
  VERY_LOW:  'var(--color-destructive)',
  LOW:       '#f97316',
  MEDIUM:    '#f59e0b',
  GOOD:      '#84cc16',
  VERY_GOOD: 'var(--color-emerald-500)',
  EXCELLENT: '#06b6d4',
  PREMIUM:   'var(--color-primary)',
};

/** Libellé français du niveau. */
const LEVEL_LABELS: Record<AIScoreLevel, string> = {
  VERY_LOW:  'Très faible',
  LOW:       'Faible',
  MEDIUM:    'Moyen',
  GOOD:      'Bon',
  VERY_GOOD: 'Très bon',
  EXCELLENT: 'Excellent',
  PREMIUM:   'Premium',
};

/** État OCR pour un document. */
interface DocState {
  file: File | null;
  uploading: boolean;
  result: OcrResult | null;
  error: string | null;
}

function emptyDoc(): DocState {
  return { file: null, uploading: false, result: null, error: null };
}

@Component({
  selector: 'app-score-request',
  standalone: true,
  imports: [
    DecimalPipe, NgFor, NgIf, NgStyle, NgSwitch, NgSwitchCase,
    ReactiveFormsModule,
    ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent,
  ],
  templateUrl: './score-request.component.html',
  styleUrl:    './score-request.component.css',
})
export class ScoreRequestComponent {
  private readonly fb             = inject(FormBuilder);
  private readonly aiScoreService = inject(AiScoreService);
  private readonly auth           = inject(AuthService);
  private readonly router         = inject(Router);

  readonly step    = signal(1);
  readonly loading = signal(false);
  readonly error   = signal<string | null>(null);
  readonly result  = signal<AIScoreResponse | null>(null);

  /** États OCR pour chaque document. */
  readonly stegDoc   = signal<DocState>(emptyDoc());
  readonly sonodeDoc = signal<DocState>(emptyDoc());
  readonly cinDoc    = signal<DocState>(emptyDoc());

  readonly form = this.fb.group({
    monthlySalary:   [null as number | null, [Validators.required, Validators.min(1)]],
    stegPaidOnTime:  [false as boolean],
    sondePaidOnTime: [false as boolean],
    cinVerified:     [false as boolean],
  });

  // ── Helpers résultat ────────────────────────────────────────────

  readonly levelColor = computed(() => {
    const r = this.result();
    return r ? (LEVEL_COLORS[r.scoreLevel] ?? 'var(--color-primary)') : 'var(--color-primary)';
  });

  levelLabel(level: AIScoreLevel): string { return LEVEL_LABELS[level] ?? level; }

  scorePercent(score: number): number { return Math.round((score / 1000) * 100); }

  levelTone(level: AIScoreLevel): 'success' | 'info' | 'warning' | 'danger' | 'muted' {
    switch (level) {
      case 'PREMIUM': case 'EXCELLENT': case 'VERY_GOOD': return 'success';
      case 'GOOD':    return 'info';
      case 'MEDIUM':  return 'warning';
      case 'LOW':     return 'warning';
      case 'VERY_LOW': return 'danger';
    }
  }

  scoreDetailsEntries(res: AIScoreResponse): (AIScoreDetailItem & { key: string })[] {
    if (!res.scoreDetails) return [];
    return Object.entries(res.scoreDetails).map(([key, item]) => ({ key, ...item }));
  }

  // ── OCR : upload d'un document ───────────────────────────────────

  onFileSelected(event: Event, docType: 'STEG' | 'SONEDE' | 'CIN'): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;
    this.uploadDocument(file, docType);
  }

  onFileDrop(event: DragEvent, docType: 'STEG' | 'SONEDE' | 'CIN'): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file) this.uploadDocument(file, docType);
  }

  onDragOver(event: DragEvent): void { event.preventDefault(); }

  private uploadDocument(file: File, docType: 'STEG' | 'SONEDE' | 'CIN'): void {
    const patch: Partial<DocState> = { file, uploading: true, result: null, error: null };
    this._patchDoc(docType, patch);

    this.aiScoreService.verifyDocument(file, docType).subscribe({
      next: (ocr) => {
        this._patchDoc(docType, { uploading: false, result: ocr });
        // Met à jour le formulaire selon le résultat OCR
        if (docType === 'STEG')   this.form.patchValue({ stegPaidOnTime:  ocr.paid_on_time ?? false });
        if (docType === 'SONEDE') this.form.patchValue({ sondePaidOnTime: ocr.paid_on_time ?? false });
        if (docType === 'CIN')    this.form.patchValue({ cinVerified:     ocr.verified ?? false });
      },
      error: (err) => {
        const msg = err?.error?.detail ?? 'Erreur OCR — vérifiez que Python tourne.';
        this._patchDoc(docType, { uploading: false, error: msg });
      },
    });
  }

  private _patchDoc(docType: 'STEG' | 'SONEDE' | 'CIN', patch: Partial<DocState>): void {
    if (docType === 'STEG')   this.stegDoc.update(s => ({ ...s, ...patch }));
    if (docType === 'SONEDE') this.sonodeDoc.update(s => ({ ...s, ...patch }));
    if (docType === 'CIN')    this.cinDoc.update(s => ({ ...s, ...patch }));
  }

  /** Permet à l'utilisateur de corriger manuellement l'OCR. */
  toggleManual(field: 'stegPaidOnTime' | 'sondePaidOnTime' | 'cinVerified'): void {
    const current = this.form.get(field)?.value as boolean;
    this.form.patchValue({ [field]: !current });
  }

  // ── Stepper ──────────────────────────────────────────────────────

  canGoNext(): boolean {
    if (this.step() === 1) {
      return (this.form.get('monthlySalary')?.valid ?? false);
    }
    return true;
  }

  next(): void { if (this.step() < 4 && this.canGoNext()) this.step.update(s => s + 1); }
  back(): void { if (this.step() > 1) this.step.update(s => s - 1); }

  // ── Soumission ────────────────────────────────────────────────────

  submit(): void {
    if (this.form.invalid) return;

    const user     = this.auth.currentUser();
    const clientId = user ? Number(user.id) : 1;
    const { monthlySalary, stegPaidOnTime, sondePaidOnTime, cinVerified } = this.form.value;

    this.loading.set(true);
    this.error.set(null);

    this.aiScoreService.calculateScore(clientId, {
      monthlySalary:   monthlySalary   ?? 0,
      stegPaidOnTime:  stegPaidOnTime  ?? false,
      sondePaidOnTime: sondePaidOnTime ?? false,
      cinVerified:     cinVerified     ?? false,
    }).subscribe({
      next:  (res) => { this.result.set(res); this.loading.set(false); },
      error: ()    => {
        this.error.set('Erreur lors du calcul. Vérifiez que Spring Boot et Python sont démarrés.');
        this.loading.set(false);
      },
    });
  }

  reset(): void {
    this.result.set(null);
    this.error.set(null);
    this.step.set(1);
    this.stegDoc.set(emptyDoc());
    this.sonodeDoc.set(emptyDoc());
    this.cinDoc.set(emptyDoc());
    this.form.reset({ stegPaidOnTime: false, sondePaidOnTime: false, cinVerified: false });
  }

  goToCredit(): void { void this.router.navigateByUrl('/dashboard/credit'); }
}
