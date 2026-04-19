import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsuranceProductService } from '../../../shared/services/insurance-product.service';
import { InsuranceProduct } from '../../../shared/models/insurance.models';
import { POLICY_TYPE_OPTIONS } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-product-form',
  standalone: true,
  imports: [RouterLink, FormsModule, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './product-form.component.html',
  styleUrl: './product-form.component.css',
})
export class ProductFormComponent implements OnInit {
  private readonly svc = inject(InsuranceProductService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly policyTypes = POLICY_TYPE_OPTIONS;
  isEdit = signal(false);
  loading = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);

  form: InsuranceProduct = {
    productName: '',
    policyType: 'HEALTH',
    description: '',
    premiumAmount: 0,
    coverageLimit: 0,
    durationMonths: 12,
    isActive: true,
  };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.loading.set(true);
      this.svc.getById(+id).subscribe({
        next: (p) => { this.form = { ...p }; this.loading.set(false); },
        error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
      });
    }
  }

  submit(): void {
    this.saving.set(true);
    this.error.set(null);
    const op$ = this.isEdit() ? this.svc.update(this.form) : this.svc.create(this.form);
    op$.subscribe({
      next: () => this.router.navigateByUrl('/dashboard/insurance/products'),
      error: (e) => { this.error.set(e.error?.message ?? e.message ?? 'Save failed.'); this.saving.set(false); },
    });
  }
}
