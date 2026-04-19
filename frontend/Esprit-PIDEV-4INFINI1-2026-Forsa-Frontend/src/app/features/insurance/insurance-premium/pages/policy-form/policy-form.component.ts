import { Component, inject, OnInit, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { InsuranceProductService } from '../../../shared/services/insurance-product.service';
import { InsuranceProduct } from '../../../shared/models/insurance.models';
import { InsurancePolicyApplicationDTO } from '../../../shared/models/insurance.models';
import { PAYMENT_FREQUENCY_OPTIONS } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-policy-form',
  standalone: true,
  imports: [RouterLink, FormsModule, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './policy-form.component.html',
  styleUrl: './policy-form.component.css',
})
export class PolicyFormComponent implements OnInit {
  private readonly policySvc = inject(InsurancePolicyService);
  private readonly productSvc = inject(InsuranceProductService);
  private readonly router = inject(Router);

  readonly frequencies = PAYMENT_FREQUENCY_OPTIONS;
  products = signal<InsuranceProduct[]>([]);
  loadingProducts = signal(true);
  saving = signal(false);
  error = signal<string | null>(null);

  form: InsurancePolicyApplicationDTO = {
    productId: 0,
    desiredCoverage: 100000,
    durationMonths: 12,
    paymentFrequency: 'MONTHLY'
  };


  ngOnInit(): void {
    this.productSvc.getAll().subscribe({
      next: (list) => { this.products.set(list.filter((p) => p.isActive)); this.loadingProducts.set(false); },
      error: () => this.loadingProducts.set(false),
    });
  }

  submit(): void {
    if (!this.form.productId) { this.error.set('Please select a product.'); return; }
    this.saving.set(true);
    this.error.set(null);
    this.policySvc.clientApply(this.form).subscribe({
      next: () => this.router.navigateByUrl('/dashboard/insurance/policies'),
      error: (e) => { this.error.set(e.error?.message ?? e.message ?? 'Submission failed.'); this.saving.set(false); },
    });
  }
}
