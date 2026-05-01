import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import type { PartnerTransaction } from '../../../core/models/forsa.models';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { PartnerService } from '../services/partner.service';

@Component({
  selector: 'app-qr-scan',
  standalone: true,
  imports: [FormsModule, RouterLink, DecimalPipe, ForsaCardComponent, ForsaButtonComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './qr-scan.component.html',
  styleUrl: './qr-scan.component.css',
})
export class QrScanComponent {
  private readonly auth = inject(AuthService);
  private readonly partnerService = inject(PartnerService);
  private readonly router = inject(Router);

  // Form inputs — plain properties so [(ngModel)] works
  qrCodeId = '';
  amount: number | null = null;
  description = '';

  // Reactive state
  submitting = signal(false);
  result = signal<PartnerTransaction | null>(null);
  errorMessage = signal<string | null>(null);

  get canSubmit(): boolean {
    return !!this.qrCodeId.trim() && (this.amount ?? 0) > 0;
  }

  submit(): void {
    const user = this.auth.currentUser();
    if (!user?.id || !this.canSubmit) return;
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.result.set(null);
    this.partnerService.createTransaction({
  clientId: Number(user.id),
  qrSessionId: this.qrCodeId.trim(),
  amount: this.amount!,
  description: this.description,
}).subscribe({
      next: (tx) => { this.result.set(tx); this.submitting.set(false); },
      error: () => {
        this.errorMessage.set('Transaction failed. Please check the QR code and try again.');
        this.submitting.set(false);
      },
    });
  }

  reset(): void {
    this.result.set(null);
    this.errorMessage.set(null);
    this.qrCodeId = '';
    this.amount = null;
    this.description = '';
  }

  viewHistory(): void {
    this.router.navigate(['/dashboard/partenariat/my-transactions']);
  }
}
