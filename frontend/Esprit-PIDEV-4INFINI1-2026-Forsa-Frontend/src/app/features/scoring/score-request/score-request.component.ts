import { DecimalPipe, NgFor, NgIf, NgStyle, NgSwitch, NgSwitchCase } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import type { AIScoreDto, AIScoreLevel } from '../../../core/models/forsa.models';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { AiScoreService, type OcrResult } from '../services/ai-score.service';

const LEVEL_COLORS: Record<AIScoreLevel, string> = {
  VERY_LOW:  'var(--color-destructive)',
  LOW:       '#f97316',
  MEDIUM:    '#f59e0b',
  GOOD:      '#84cc16',
  VERY_GOOD: 'var(--color-emerald-500)',
  EXCELLENT: '#06b6d4',
  PREMIUM:   'var(--color-primary)',
};

const LEVEL_LABELS: Record<AIScoreLevel, string> = {
  VERY_LOW:  'Very low',
  LOW:       'Low',
  MEDIUM:    'Medium',
  GOOD:      'Good',
  VERY_GOOD: 'Very good',
  EXCELLENT: 'Excellent',
  PREMIUM:   'Premium',
};

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
    ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent,
  ],
  templateUrl: './score-request.component.html',
  styleUrl:    './score-request.component.css',
})
export class ScoreRequestComponent {
  private readonly aiScoreService = inject(AiScoreService);
  private readonly auth           = inject(AuthService);
  private readonly router         = inject(Router);

  readonly step    = signal(1);
  readonly loading = signal(false);
  readonly error   = signal<string | null>(null);
  readonly result  = signal<AIScoreDto | null>(null);

  /** Pay slip — mandatory, salary extracted via OCR. */
  readonly salaryDoc  = signal<DocState>(emptyDoc());
  readonly ocrSalary  = signal<number | null>(null);

  /** Utility bills — optional. */
  readonly stegDoc   = signal<DocState>(emptyDoc());
  readonly sonodeDoc = signal<DocState>(emptyDoc());

  readonly levelColor = computed(() => {
    const r = this.result();
    return r ? (LEVEL_COLORS[r.scoreLevel] ?? 'var(--color-primary)') : 'var(--color-primary)';
  });

  levelLabel(level: AIScoreLevel): string { return LEVEL_LABELS[level] ?? level; }
  scorePercent(score: number): number     { return Math.round((score / 1000) * 100); }

  levelTone(level: AIScoreLevel): 'success' | 'info' | 'warning' | 'danger' | 'muted' {
    switch (level) {
      case 'PREMIUM': case 'EXCELLENT': case 'VERY_GOOD': return 'success';
      case 'GOOD':    return 'info';
      case 'MEDIUM':  return 'warning';
      case 'LOW':     return 'warning';
      case 'VERY_LOW': return 'danger';
    }
  }

  // ── OCR upload ───────────────────────────────────────────────────────────

  onFileSelected(event: Event, docType: 'SALARY' | 'STEG' | 'SONEDE'): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;
    this.uploadDocument(file, docType);
  }

  onFileDrop(event: DragEvent, docType: 'SALARY' | 'STEG' | 'SONEDE'): void {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0];
    if (file) this.uploadDocument(file, docType);
  }

  onDragOver(event: DragEvent): void { event.preventDefault(); }

  private uploadDocument(file: File, docType: 'SALARY' | 'STEG' | 'SONEDE'): void {
    this._patchDoc(docType, { file, uploading: true, result: null, error: null });

    this.aiScoreService.verifyDocument(file, docType).subscribe({
      next: (ocr) => {
        this._patchDoc(docType, { uploading: false, result: ocr });
        if (docType === 'SALARY') {
          if (ocr.salary && ocr.salary > 0) {
            this.ocrSalary.set(ocr.salary);
          } else {
            this._patchDoc('SALARY', { error: 'Salary amount not detected. Please try a clearer image.' });
            this.ocrSalary.set(null);
          }
        }
      },
      error: (err) => {
        const msg = err?.error?.detail ?? 'Could not process this document. Please try again.';
        this._patchDoc(docType, { uploading: false, error: msg });
      },
    });
  }

  private _patchDoc(docType: 'SALARY' | 'STEG' | 'SONEDE', patch: Partial<DocState>): void {
    if (docType === 'SALARY') this.salaryDoc.update(s => ({ ...s, ...patch }));
    if (docType === 'STEG')   this.stegDoc.update(s  => ({ ...s, ...patch }));
    if (docType === 'SONEDE') this.sonodeDoc.update(s => ({ ...s, ...patch }));
  }

  // ── Stepper ──────────────────────────────────────────────────────────────

  canGoNext(): boolean {
    if (this.step() === 1) {
      return this.ocrSalary() != null && (this.ocrSalary() ?? 0) > 0;
    }
    return true;
  }

  next(): void { if (this.step() < 4 && this.canGoNext()) this.step.update(s => s + 1); }
  back(): void { if (this.step() > 1) this.step.update(s => s - 1); }

  // ── Submit ────────────────────────────────────────────────────────────────

  submit(): void {
    const salary = this.ocrSalary();
    if (!salary || salary <= 0) {
      this.error.set('Please upload your pay slip first.');
      return;
    }

    const user     = this.auth.currentUser();
    const clientId = user ? Number(user.id) : 1;
    const stegPaid   = this.stegDoc().result?.paid_on_time   ?? false;
    const sonedePaid = this.sonodeDoc().result?.paid_on_time ?? false;

    this.loading.set(true);
    this.error.set(null);

    this.aiScoreService.submitFirstScore(clientId, salary, stegPaid, sonedePaid).subscribe({
      next:  (res) => { this.result.set(res); this.loading.set(false); },
      error: ()    => {
        this.error.set('Could not calculate the score. Ensure Spring Boot and the Python service are running.');
        this.loading.set(false);
      },
    });
  }

  reset(): void {
    this.result.set(null);
    this.error.set(null);
    this.step.set(1);
    this.salaryDoc.set(emptyDoc());
    this.stegDoc.set(emptyDoc());
    this.sonodeDoc.set(emptyDoc());
    this.ocrSalary.set(null);
  }

  goToCredit(): void { void this.router.navigateByUrl('/dashboard/credit'); }
}
