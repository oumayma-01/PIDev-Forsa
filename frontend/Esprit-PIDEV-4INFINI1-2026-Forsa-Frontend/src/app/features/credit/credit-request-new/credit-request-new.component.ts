import { Component, ElementRef, OnDestroy, ViewChild, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import type { AmortizationType } from '../../../core/models/credit-api.model';
import { CreditApiService } from '../../../core/services/credit-api.service';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

@Component({
  selector: 'app-credit-request-new',
  standalone: true,
  imports: [FormsModule, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './credit-request-new.component.html',
  styleUrl: './credit-request-new.component.css',
})
export class CreditRequestNewComponent implements OnDestroy {
  private readonly api = inject(CreditApiService);
  private readonly router = inject(Router);

  // ── Step control ─────────────────────────────────────────────────────────────
  readonly step = signal<1 | 2>(1);

  // ── Step 1 fields ────────────────────────────────────────────────────────────
  amountRequested: number | null = null;
  durationMonths: number | null = null;
  typeCalcul: AmortizationType = 'AMORTISSEMENT_CONSTANT';
  private healthReportFile: File | null = null;
  readonly healthReportName = signal<string | null>(null);

  // ── Step 2 – Guarantor fields ─────────────────────────────────────────────────
  guarantorName = '';
  guarantorCin = '';
  guarantorBankAccount = '';
  private guarantorPhotoFile: File | null = null;
  readonly guarantorPhotoPreview = signal<string | null>(null);

  // ── Camera ───────────────────────────────────────────────────────────────────
  readonly cameraActive = signal(false);
  readonly cameraError = signal<string | null>(null);
  private mediaStream: MediaStream | null = null;

  @ViewChild('videoEl') videoElRef!: ElementRef<HTMLVideoElement>;
  @ViewChild('canvasEl') canvasElRef!: ElementRef<HTMLCanvasElement>;

  // ── Global state ─────────────────────────────────────────────────────────────
  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  // ── Step 1 handlers ───────────────────────────────────────────────────────────
  onHealthReportSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    this.error.set(null);
    if (!file) return;
    const okType = file.type === 'application/pdf' || file.type.startsWith('image/');
    if (!okType) { this.error.set('Medical report must be a PDF or an image.'); return; }
    if (file.size > 10 * 1024 * 1024) { this.error.set('File too large (max 10 MB).'); return; }
    this.healthReportFile = file;
    this.healthReportName.set(file.name);
  }

  nextStep(): void {
    this.error.set(null);
    const amount = this.amountRequested ?? 0;
    const duration = this.durationMonths ?? 0;
    if (!Number.isFinite(amount) || amount <= 0) { this.error.set('Please enter a valid amount.'); return; }
    if (!Number.isFinite(duration) || duration <= 0) { this.error.set('Please enter a valid duration.'); return; }
    if (!this.healthReportFile) { this.error.set('Please attach the medical report.'); return; }
    this.step.set(2);
  }

  prevStep(): void {
    this.stopCamera();
    this.error.set(null);
    this.step.set(1);
  }

  // ── Camera handlers ───────────────────────────────────────────────────────────
  async startCamera(): Promise<void> {
    this.cameraError.set(null);
    try {
      this.mediaStream = await navigator.mediaDevices.getUserMedia({ video: { facingMode: 'environment' } });
      this.cameraActive.set(true);
      // Small delay to ensure the <video> element is in DOM
      setTimeout(() => {
        if (this.videoElRef?.nativeElement && this.mediaStream) {
          this.videoElRef.nativeElement.srcObject = this.mediaStream;
        }
      }, 100);
    } catch {
      this.cameraError.set("Unable to access camera. Please allow access in your browser.");
    }
  }

  capturePhoto(): void {
    const video = this.videoElRef?.nativeElement;
    const canvas = this.canvasElRef?.nativeElement;
    if (!video || !canvas) return;

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.drawImage(video, 0, 0);

    const dataUrl = canvas.toDataURL('image/jpeg', 0.9);
    this.guarantorPhotoPreview.set(dataUrl);

    canvas.toBlob((blob) => {
      if (blob) {
        this.guarantorPhotoFile = new File([blob], 'guarantor-cin-photo.jpg', { type: 'image/jpeg' });
      }
    }, 'image/jpeg', 0.9);

    this.stopCamera();
  }

  retakePhoto(): void {
    this.guarantorPhotoPreview.set(null);
    this.guarantorPhotoFile = null;
    void this.startCamera();
  }

  stopCamera(): void {
    this.mediaStream?.getTracks().forEach((t) => t.stop());
    this.mediaStream = null;
    this.cameraActive.set(false);
  }

  // ── Submit ────────────────────────────────────────────────────────────────────
  submit(): void {
    this.error.set(null);
    this.message.set(null);

    if (!this.guarantorName.trim()) { this.error.set("Please enter the guarantor's full name."); return; }
    if (!this.guarantorCin.trim()) { this.error.set("Please enter the guarantor's CIN number."); return; }
    if (!this.guarantorBankAccount.trim()) { this.error.set("Please enter the guarantor's bank account."); return; }
    if (!this.guarantorPhotoFile) { this.error.set("Please capture the guarantor's photo with their CIN card."); return; }

    this.busy.set(true);
    this.api
      .createCreditWithHealthReport({
        amountRequested: this.amountRequested!,
        durationMonths: this.durationMonths!,
        typeCalcul: this.typeCalcul,
        healthReport: this.healthReportFile!,
        guarantorName: this.guarantorName,
        guarantorCin: this.guarantorCin,
        guarantorBankAccount: this.guarantorBankAccount,
        guarantorPhoto: this.guarantorPhotoFile,
      })
      .subscribe({
        next: (created) => {
          this.busy.set(false);
          this.message.set(`Request created successfully (ID=${created.id}).`);
          void this.router.navigateByUrl(`/dashboard/credit/${created.id}`);
        },
        error: (err) => {
          this.busy.set(false);
          this.error.set(this.readError(err));
        },
      });
  }

  ngOnDestroy(): void {
    this.stopCamera();
  }

  private readError(err: unknown): string {
    const body = (err as { error?: { message?: string; error?: string } })?.error;
    if (typeof body?.message === 'string' && body.message.trim()) return body.message;
    if (typeof body?.error === 'string' && body.error.trim()) return body.error;
    return 'Error creating request. Please try again.';
  }
}
