import { Component, inject, signal } from '@angular/core';
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
export class CreditRequestNewComponent {
  private readonly api = inject(CreditApiService);
  private readonly router = inject(Router);

  amountRequested: number | null = null;
  durationMonths: number | null = null;
  typeCalcul: AmortizationType = 'AMORTISSEMENT_CONSTANT';

  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly message = signal<string | null>(null);

  private healthReportFile: File | null = null;
  readonly healthReportName = signal<string | null>(null);

  onHealthReportSelected(ev: Event): void {
    const input = ev.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';

    this.error.set(null);
    this.message.set(null);

    if (!file) {
      return;
    }

    const okType = file.type === 'application/pdf' || file.type.startsWith('image/');
    if (!okType) {
      this.error.set('Le rapport médical doit être un PDF ou une image.');
      return;
    }

    // Soft limit to prevent huge uploads from the UI.
    if (file.size > 10 * 1024 * 1024) {
      this.error.set('Fichier trop volumineux (max 10 MB).');
      return;
    }

    this.healthReportFile = file;
    this.healthReportName.set(file.name);
  }

  submit(): void {
    this.error.set(null);
    this.message.set(null);

    const amount = this.amountRequested ?? 0;
    const duration = this.durationMonths ?? 0;

    if (!Number.isFinite(amount) || amount <= 0) {
      this.error.set('Veuillez saisir un montant valide.');
      return;
    }
    if (!Number.isFinite(duration) || duration <= 0) {
      this.error.set('Veuillez saisir une durée valide.');
      return;
    }
    if (!this.healthReportFile) {
      this.error.set('Veuillez joindre le rapport médical (PDF ou image).');
      return;
    }

    this.busy.set(true);
    this.api
      .createCreditWithHealthReport({
        amountRequested: amount,
        durationMonths: duration,
        typeCalcul: this.typeCalcul,
        healthReport: this.healthReportFile,
      })
      .subscribe({
        next: (created) => {
          this.busy.set(false);
          this.message.set(`Demande créée (ID=${created.id}).`);
          void this.router.navigateByUrl(`/dashboard/credit/${created.id}`);
        },
        error: (err) => {
          this.busy.set(false);
          this.error.set(this.readError(err));
        },
      });
  }

  private readError(err: unknown): string {
    const body = (err as { error?: { message?: string; error?: string } })?.error;
    if (typeof body?.message === 'string' && body.message.trim()) {
      return body.message;
    }
    if (typeof body?.error === 'string' && body.error.trim()) {
      return body.error;
    }
    return 'Erreur lors de la création de la demande. Veuillez réessayer.';
  }
}
