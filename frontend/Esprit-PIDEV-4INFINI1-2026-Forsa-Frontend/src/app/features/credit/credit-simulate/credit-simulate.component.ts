import { DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import type { AmortizationScheduleResponse, AmortizationType } from '../../../core/models/credit-api.model';
import { CreditApiService } from '../../../core/services/credit-api.service';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';

@Component({
  selector: 'app-credit-simulate',
  standalone: true,
  imports: [FormsModule, DecimalPipe, ForsaButtonComponent, ForsaCardComponent, ForsaInputDirective],
  templateUrl: './credit-simulate.component.html',
  styleUrl: './credit-simulate.component.css',
})
export class CreditSimulateComponent {
  private readonly api = inject(CreditApiService);

  principal = 10_000;
  rate = 5;
  duration = 12;
  type: AmortizationType = 'AMORTISSEMENT_CONSTANT';

  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<AmortizationScheduleResponse | null>(null);

  simulate(): void {
    this.error.set(null);
    this.result.set(null);

    if (!Number.isFinite(this.principal) || this.principal <= 0) {
      this.error.set('Veuillez saisir un montant valide.');
      return;
    }
    if (!Number.isFinite(this.rate) || this.rate <= 0) {
      this.error.set('Veuillez saisir un taux valide.');
      return;
    }
    if (!Number.isFinite(this.duration) || this.duration <= 0) {
      this.error.set('Veuillez saisir une durée valide.');
      return;
    }

    this.loading.set(true);
    this.api
      .simulateAmortization({
        principal: this.principal,
        rate: this.rate,
        duration: this.duration,
        type: this.type,
      })
      .subscribe({
        next: (res) => {
          this.result.set(res);
          this.loading.set(false);
        },
        error: (err) => {
          this.loading.set(false);
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
    return 'Erreur lors de la simulation. Veuillez réessayer.';
  }
}
