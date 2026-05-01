import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import type { PartnerType } from '../../../core/models/forsa.models';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { PARTNER_TYPE_LABELS } from '../partenariat-list/partenariat-type-label-public';
import { PartnerService } from '../services/partner.service';

@Component({
  selector: 'app-partner-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, ForsaCardComponent, ForsaButtonComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './partner-form.component.html',
  styleUrl: './partner-form.component.css',
})
export class PartnerFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly partnerService = inject(PartnerService);

  readonly isEdit = signal(false);
  readonly partnerId = signal<number | null>(null);
  readonly saving = signal(false);
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);

  readonly typeOptions = Object.entries(PARTNER_TYPE_LABELS) as [PartnerType, string][];

  readonly form = this.fb.nonNullable.group({
    businessName: ['', Validators.required],
    partnerType: ['' as PartnerType, Validators.required],
    registrationNumber: ['', Validators.required],
    address: ['', Validators.required],
    city: ['', Validators.required],
    businessPhone: [''],
    businessEmail: ['', Validators.email],
    description: [''],
    iban: ['', Validators.required],
    bankName: ['', Validators.required],
    accountHolderName: ['', Validators.required],
    maxTransactionAmount: [1000, [Validators.required, Validators.min(1)]],
    dailyTransactionLimit: [5000, [Validators.required, Validators.min(1)]],
    monthlyTransactionLimit: [20000, [Validators.required, Validators.min(1)]],
    commissionRate: [2.5, [Validators.required, Validators.min(0), Validators.max(100)]],
    contactPersonName: [''],
    contactPersonPhone: [''],
    contactPersonEmail: ['', Validators.email],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.partnerId.set(Number(id));
      this.loading.set(true);
      this.partnerService.getPartnerById(Number(id)).subscribe({
        next: (p) => { this.form.patchValue(p as any); this.loading.set(false); },
        error: () => { this.errorMessage.set('Could not load partner data.'); this.loading.set(false); },
      });
    }
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving.set(true);
    this.errorMessage.set(null);
    const data = this.form.value;
    const req = this.isEdit()
      ? this.partnerService.updatePartner(this.partnerId()!, data)
      : this.partnerService.createPartner(data);
    req.subscribe({
      next: () => { this.saving.set(false); this.router.navigate(['/dashboard/partenariat']); },
      error: () => { this.errorMessage.set('An error occurred. Please try again.'); this.saving.set(false); },
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard/partenariat']);
  }

  hasError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
}
