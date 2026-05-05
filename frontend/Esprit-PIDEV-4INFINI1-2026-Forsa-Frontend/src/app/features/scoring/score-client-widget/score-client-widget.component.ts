import { DecimalPipe, NgClass } from '@angular/common';
import { Component, ElementRef, OnDestroy, OnInit, ViewChild, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AiScoreService, OcrResult } from '../services/ai-score.service';
import type { AIScoreDto } from '../../../core/models/forsa.models';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

interface DocWidgetState {
  uploading: boolean;
  result: OcrResult | null;
  error: string | null;
  fileName: string | null;
}

function emptyDoc(): DocWidgetState {
  return { uploading: false, result: null, error: null, fileName: null };
}

@Component({
  selector: 'app-score-client-widget',
  standalone: true,
  imports: [DecimalPipe, NgClass, FormsModule, ForsaCardComponent, ForsaButtonComponent, ForsaIconComponent],
  templateUrl: './score-client-widget.component.html',
  styleUrl: './score-client-widget.component.css',
})
export class ScoreClientWidgetComponent implements OnInit, OnDestroy {
  // STATE 2 booster inputs
  @ViewChild('stegInput')   stegInputRef!:   ElementRef<HTMLInputElement>;
  @ViewChild('sonedeInput') sonedeInputRef!: ElementRef<HTMLInputElement>;

  // STATE 1 new-client inputs
  @ViewChild('salaryInput')    salaryInputRef!:    ElementRef<HTMLInputElement>;
  @ViewChild('stegNewInput')   stegNewInputRef!:   ElementRef<HTMLInputElement>;
  @ViewChild('sonedeNewInput') sonedeNewInputRef!: ElementRef<HTMLInputElement>;

  // ── Global ───────────────────────────────────────────────────────────────
  checking = signal(true);
  hasScore = signal(false);
  loading  = signal(false);
  error    = signal(false);
  score    = signal<AIScoreDto | null>(null);

  // ── STATE 2: booster ─────────────────────────────────────────────────────
  docLoading  = signal<'STEG' | 'SONEDE' | null>(null);
  billMessage = signal<{ type: 'success' | 'error'; text: string } | null>(null);

  // ── STATE 1: new-client docs ──────────────────────────────────────────────
  stegNewDoc   = signal<DocWidgetState>(emptyDoc());
  sonedeNewDoc = signal<DocWidgetState>(emptyDoc());
  salaryDoc    = signal<DocWidgetState>(emptyDoc());
  ocrSalary    = signal<number | null>(null);
  // Fallback manual salary when OCR fails
  manualSalary    = signal<number | null>(null);
  showManualEntry = signal(false);

  firstScoreLoading = signal(false);
  firstScoreError   = signal<string | null>(null);

  // ── Computed: effective salary (OCR preferred, manual as fallback) ────────
  effectiveSalary = computed(() => this.ocrSalary() ?? this.manualSalary());

  // ── Gauge SVG: semicircle length ≈ π × 85 ≈ 267 ─────────────────────────
  gaugeDash = computed(() => {
    const s = this.score();
    if (!s) return 0;
    const pct = Math.min(1, Math.max(0, (s.currentScore ?? s.score ?? 0) / 1000));
    return Math.round(pct * 267);
  });

  gaugeColor = computed(() => {
    const s = this.score();
    const lvl = String(s?.scoreLevel ?? '').toUpperCase();
    if (['VERY_LOW', 'LOW'].includes(lvl)) return '#ef4444';
    if (lvl === 'MEDIUM')                  return '#f59e0b';
    if (lvl === 'GOOD')                    return '#84cc16';
    if (lvl === 'VERY_GOOD')               return '#22c55e';
    if (lvl === 'EXCELLENT')               return '#2563eb';
    if (lvl === 'PREMIUM')                 return '#7c3aed';
    return '#6b7280';
  });

  private refreshTimer: ReturnType<typeof setInterval> | null = null;

  constructor(private readonly auth: AuthService, private readonly aiScore: AiScoreService) {}

  async ngOnInit(): Promise<void> {
    await this.auth.ensureSessionFromApi();
    this.checkStatus();
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

  checkStatus(): void {
    const cid = this.clientId();
    if (cid == null) { this.checking.set(false); return; }
    this.error.set(false);
    this.checking.set(true);

    this.aiScore.getScoreStatus(cid).subscribe({
      next: (status) => {
        this.checking.set(false);
        if (status.hasScore) {
          this.hasScore.set(true);
          this.score.set(status as unknown as AIScoreDto);
          if (!this.refreshTimer) {
            this.refreshTimer = setInterval(() => this.loadScore(), 60_000);
          }
        } else {
          this.hasScore.set(false);
        }
      },
      error: () => { this.checking.set(false); this.error.set(true); },
    });
  }

  loadScore(): void {
    const cid = this.clientId();
    if (cid == null) return;
    this.loading.set(true);
    this.aiScore.getCurrentScore(cid).subscribe({
      next: (s) => { this.score.set(s); this.loading.set(false); },
      error: ()  => this.loading.set(false),
    });
  }

  // ── STATE 1: new-client uploads ──────────────────────────────────────────

  triggerUpload(ref: ElementRef<HTMLInputElement>): void {
    if (this.firstScoreLoading()) return;
    ref.nativeElement.click();
  }

  onDocSelected(event: Event, type: 'STEG' | 'SONEDE' | 'SALARY'): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;

    const stateMap: Record<'STEG' | 'SONEDE' | 'SALARY', (s: DocWidgetState) => void> = {
      STEG:   (s) => this.stegNewDoc.set(s),
      SONEDE: (s) => this.sonedeNewDoc.set(s),
      SALARY: (s) => this.salaryDoc.set(s),
    };
    const set = stateMap[type];

    set({ uploading: true, result: null, error: null, fileName: file.name });
    if (type === 'SALARY') { this.ocrSalary.set(null); this.showManualEntry.set(false); }

    this.aiScore.verifyDocument(file, type).subscribe({
      next: (ocr) => {
        set({ uploading: false, result: ocr, error: null, fileName: file.name });
        if (type === 'SALARY') {
          if (ocr.salary && ocr.salary > 0) {
            this.ocrSalary.set(ocr.salary);
          } else {
            // OCR didn't detect salary — offer manual fallback
            this.showManualEntry.set(true);
            set({ uploading: false, result: ocr, error: 'Salary not detected automatically.', fileName: file.name });
          }
        }
        input.value = '';
      },
      error: () => {
        if (type === 'SALARY') {
          // OCR service unreachable — offer manual fallback
          this.showManualEntry.set(true);
          set({ uploading: false, result: null, error: 'Document could not be read automatically.', fileName: file.name });
        } else {
          set({ uploading: false, result: null, error: 'Could not process document. Proceeding without it.', fileName: null });
        }
        input.value = '';
      },
    });
  }

  calculateFirstScore(): void {
    const cid    = this.clientId();
    const salary = this.effectiveSalary();
    if (cid == null || !salary || salary <= 0) return;

    const stegPaid   = this.stegNewDoc().result?.paid_on_time   ?? false;
    const sonedePaid = this.sonedeNewDoc().result?.paid_on_time ?? false;

    this.firstScoreLoading.set(true);
    this.firstScoreError.set(null);

    this.aiScore.submitFirstScore(cid, salary, stegPaid, sonedePaid).subscribe({
      next: (result) => {
        this.score.set(result);
        this.hasScore.set(true);
        this.firstScoreLoading.set(false);
        if (!this.refreshTimer) {
          this.refreshTimer = setInterval(() => this.loadScore(), 60_000);
        }
      },
      error: () => {
        this.firstScoreError.set('Could not calculate your score. Please check that all services are running.');
        this.firstScoreLoading.set(false);
      },
    });
  }

  // ── STATE 2: booster uploads ─────────────────────────────────────────────

  triggerBillUpload(type: 'STEG' | 'SONEDE'): void {
    if (this.docLoading()) return;
    if (type === 'STEG') this.stegInputRef.nativeElement.click();
    else                  this.sonedeInputRef.nativeElement.click();
  }

  onFileSelected(event: Event, type: 'STEG' | 'SONEDE'): void {
    const input = event.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (!file) return;
    const cid = this.clientId();
    if (cid == null) return;

    this.docLoading.set(type);
    this.billMessage.set(null);

    this.aiScore.verifyDocument(file, type).subscribe({
      next: (result: OcrResult) => {
        const paidOnTime = result.paid_on_time === true ||
          (result as unknown as { paidOnTime?: boolean }).paidOnTime === true;
        if (result.verified && paidOnTime) {
          this.aiScore.activateBooster(cid, type).subscribe({
            next: (s) => {
              this.score.set(s);
              this.docLoading.set(null);
              this.billMessage.set({ type: 'success', text: `${type} bill verified! Credit limit updated.` });
            },
            error: () => {
              this.docLoading.set(null);
              this.billMessage.set({ type: 'error', text: 'Could not activate booster. Please try again.' });
            },
          });
        } else {
          this.docLoading.set(null);
          this.billMessage.set({ type: 'error', text: 'Bill not paid on time or could not be read. Please try a clear photo.' });
        }
        input.value = '';
      },
      error: () => {
        this.docLoading.set(null);
        this.billMessage.set({ type: 'error', text: 'Could not verify document. Please try again.' });
        input.value = '';
      },
    });
  }

  recalculateScore(): void {
    const cid = this.clientId();
    if (cid == null) return;
    this.loading.set(true);
    this.aiScore.recalculateScore(cid).subscribe({
      next: (s) => { this.score.set(s); this.loading.set(false); },
      error: ()  => this.loading.set(false),
    });
  }

  // ── STATE 1 convenience triggers ────────────────────────────────────────

  triggerSalaryUpload(): void    { this.triggerUpload(this.salaryInputRef); }
  triggerStegNewUpload(): void   { this.triggerUpload(this.stegNewInputRef); }
  triggerSonedeNewUpload(): void { this.triggerUpload(this.sonedeNewInputRef); }

  onSalarySelected(event: Event): void    { this.onDocSelected(event, 'SALARY'); }
  onStegNewSelected(event: Event): void   { this.onDocSelected(event, 'STEG'); }
  onSonedeNewSelected(event: Event): void { this.onDocSelected(event, 'SONEDE'); }

  clientStandingToneClass(): string {
    const lvl = String(this.score()?.scoreLevel ?? '').toUpperCase();
    if (['VERY_LOW', 'LOW'].includes(lvl)) return 'standing-strip standing-strip--risk';
    if (lvl === 'MEDIUM')                  return 'standing-strip standing-strip--mid';
    return 'standing-strip standing-strip--ok';
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  get thresholdPercent(): number {
    const v = this.score()?.availableThreshold;
    if (v == null || v <= 0) return 0;
    return Math.min((v / 15000) * 100, 100);
  }

  clientNumericScore(): number {
    const s = this.score();
    if (!s) return 0;
    const v = Number(s.currentScore ?? s.score ?? 0);
    return Number.isFinite(v) ? Math.round(Math.min(1000, Math.max(0, v))) : 0;
  }

  clientStandingLabel(): string {
    const raw = String(this.score()?.scoreLevel ?? 'VERY_LOW').toUpperCase();
    const map: Record<string, string> = {
      VERY_LOW: 'Very Low', LOW: 'Low', MEDIUM: 'Medium', GOOD: 'Good',
      VERY_GOOD: 'Very Good', EXCELLENT: 'Excellent', PREMIUM: 'Premium',
    };
    return map[raw] ?? raw.replace(/_/g, ' ');
  }

  levelBadgeClass(): string {
    const lvl = String(this.score()?.scoreLevel ?? '').toUpperCase();
    if (lvl === 'PREMIUM')   return 'badge badge--premium';
    if (lvl === 'EXCELLENT') return 'badge badge--excellent';
    if (lvl === 'VERY_GOOD') return 'badge badge--very-good';
    if (lvl === 'GOOD')      return 'badge badge--good';
    if (lvl === 'MEDIUM')    return 'badge badge--medium';
    if (lvl === 'LOW')       return 'badge badge--low';
    return 'badge badge--very-low';
  }

  formatExpiry(dateStr: string | null | undefined): string {
    if (!dateStr) return '';
    return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  }

  formatDate(iso: string | null | undefined): string {
    if (!iso) return 'never';
    const d = new Date(iso);
    const now = new Date();
    const diffMs = now.getTime() - d.getTime();
    const diffMin = Math.floor(diffMs / 60000);
    if (diffMin < 1)  return 'just now';
    if (diffMin < 60) return `${diffMin} min ago`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24)   return `${diffH}h ago`;
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit', year: '2-digit' });
  }

  // Score breakdown segments (approximate visual proportions)
  get breakdownBase(): number {
    const s = this.clientNumericScore();
    return Math.round(s * 0.62);
  }
  get breakdownBooster(): number {
    const s = this.score();
    const active = (s?.stegBoosterActive ? 1 : 0) + (s?.sonedeBoosterActive ? 1 : 0);
    return active * 15;
  }
  get breakdownBehavior(): number {
    return Math.round(this.clientNumericScore() * 0.13);
  }
  get breakdownWallet(): number {
    return Math.round(this.clientNumericScore() * 0.10);
  }
}
