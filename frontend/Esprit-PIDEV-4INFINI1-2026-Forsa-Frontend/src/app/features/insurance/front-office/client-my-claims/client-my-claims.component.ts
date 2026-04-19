import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InsuranceClaimService } from '../../shared/services/insurance-claim.service';
import { InsuranceClaim } from '../../shared/models/insurance.models';

@Component({
  selector: 'app-client-my-claims',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  templateUrl: './client-my-claims.component.html',
  styleUrls: ['./client-my-claims.component.css']
})
export class ClientMyClaimsComponent implements OnInit {
  private readonly claimService = inject(InsuranceClaimService);
  private readonly route = inject(ActivatedRoute);
  private readonly fb = inject(FormBuilder);

  claims: InsuranceClaim[] = [];
  isLoading = true;
  
  showForm = false;
  claimForm!: FormGroup;
  isSubmitting = false;

  ngOnInit() {
    this.initForm();
    this.loadClaims();

    this.route.queryParams.subscribe(params => {
      if (params['policyId']) {
        this.showForm = true;
        this.claimForm.patchValue({
          insurancePolicy: { id: +params['policyId'] }
        });
      }
    });
  }

  loadClaims() {
    this.isLoading = true;
    this.claimService.getMyClaims().subscribe({
      next: (data) => {
        this.claims = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  initForm() {
    this.claimForm = this.fb.group({
      incidentDate: ['', Validators.required],
      claimAmount: ['', [Validators.required, Validators.min(1)]],
      description: ['', Validators.required],
      insurancePolicy: this.fb.group({
        id: ['', Validators.required]
      })
    });
  }

  toggleForm() {
    this.showForm = !this.showForm;
    if (!this.showForm) {
      this.claimForm.reset();
    }
  }

  submitClaim() {
    if (this.claimForm.invalid) return;
    this.isSubmitting = true;
    
    const newClaim: InsuranceClaim = {
      ...this.claimForm.value,
      claimDate: new Date().toISOString().split('T')[0],
      status: 'PENDING'
    };

    this.claimService.create(newClaim).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.showForm = false;
        this.claimForm.reset();
        this.loadClaims();
        alert('Claim submitted successfully!');
      },
      error: (err) => {
        console.error(err);
        this.isSubmitting = false;
        alert('Failed to submit claim.');
      }
    });
  }
}
