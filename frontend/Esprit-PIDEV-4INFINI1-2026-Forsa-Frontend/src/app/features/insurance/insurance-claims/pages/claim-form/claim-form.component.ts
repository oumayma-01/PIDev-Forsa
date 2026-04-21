import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsuranceClaimService } from '../../../shared/services/insurance-claim.service';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { InsuranceClaim, InsurancePolicy } from '../../../shared/models/insurance.models';
import { ClaimStatus } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-claim-form',
  standalone: true,
  imports: [RouterLink, FormsModule, DatePipe, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './claim-form.component.html',
  styleUrl: './claim-form.component.css',
})
export class ClaimFormComponent implements OnInit {
  private readonly claimSvc = inject(InsuranceClaimService);
  private readonly policySvc = inject(InsurancePolicyService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly claimStatuses = Object.values(ClaimStatus);
  policies = signal<InsurancePolicy[]>([]);
  isEdit = signal(false);
  loading = signal(false);
  saving = signal(false);
  error = signal<string | null>(null);

  form: InsuranceClaim = {
    claimNumber: '',
    incidentDate: '',
    claimAmount: 0,
    approvedAmount: undefined,
    description: '',
    status: ClaimStatus.PENDING,
    indemnificationPaid: undefined,
    insurancePolicy: undefined,
  };

  policyId = 0;

  ngOnInit(): void {
    // Load policies for dropdown
    this.policySvc.getAll().subscribe({
      next: (list) => this.policies.set(list),
      error: () => {},
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit.set(true);
      this.loading.set(true);
      this.claimSvc.getById(+id).subscribe({
        next: (c) => {
          this.form = { ...c };
          this.policyId = c.insurancePolicy?.id ?? 0;
          this.loading.set(false);
        },
        error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
      });
    }
  }

  submit(): void {
    this.saving.set(true);
    this.error.set(null);
    const payload: InsuranceClaim = {
      ...this.form,
      insurancePolicy: this.policyId ? { id: this.policyId } : undefined,
    };
    const op$ = this.isEdit() ? this.claimSvc.update(payload) : this.claimSvc.create(payload);
    op$.subscribe({
      next: () => this.router.navigateByUrl('/dashboard/insurance/claims'),
      error: (e) => { this.error.set(e.error?.message ?? e.message ?? 'Save failed.'); this.saving.set(false); },
    });
  }
}
