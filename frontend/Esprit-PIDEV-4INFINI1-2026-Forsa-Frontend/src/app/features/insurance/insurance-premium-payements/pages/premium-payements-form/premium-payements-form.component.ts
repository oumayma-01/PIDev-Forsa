import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { PremiumPaymentService } from '../../../shared/services/premium-payment.service';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { PremiumPayment, InsurancePolicy } from '../../../shared/models/insurance.models';
import { PaymentStatus } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-premium-payements-form',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './premium-payements-form.component.html',
  styleUrl: './premium-payements-form.component.css',
})
export class PremiumPayementsFormComponent implements OnInit {
  private readonly paySvc = inject(PremiumPaymentService);
  private readonly policySvc = inject(InsurancePolicyService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly statuses = Object.values(PaymentStatus);
  policies = signal<InsurancePolicy[]>([]);
  isEdit = signal(false);
  loading = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);

  form: PremiumPayment = {
    amount: 0,
    dueDate: '',
    paidDate: undefined,
    status: PaymentStatus.PENDING,
    transactionId: undefined,
  };

  policyId = 0;

  ngOnInit(): void {
    this.policySvc.getAll().subscribe({
      next: (list) => this.policies.set(list),
      error: () => {},
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.loading.set(true);
      this.paySvc.getById(+id).subscribe({
        next: (p) => {
          this.form = { ...p };
          this.policyId = p.insurancePolicy?.id ?? 0;
          this.loading.set(false);
        },
        error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
      });
    }
  }

  submit(): void {
    if (!this.policyId) { this.error.set('Please select a policy.'); return; }
    this.saving.set(true);
    this.error.set(null);
    const payload: PremiumPayment = {
      ...this.form,
      insurancePolicy: { id: this.policyId },
    };
    const op$ = this.isEdit() ? this.paySvc.update(payload) : this.paySvc.create(payload);
    op$.subscribe({
      next: () => this.router.navigateByUrl('/dashboard/insurance/payments'),
      error: (e) => { this.error.set(e.error?.message ?? e.message ?? 'Save failed.'); this.saving.set(false); },
    });
  }
}
