import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { ActuarialService } from '../../shared/services/actuarial.service';
import { IntegratedPolicyService } from '../../shared/services/integrated-policy.service';
import { InsuranceProductService } from '../../shared/services/insurance-product.service';
import { InsuranceCompleteQuoteDTO, InsuranceProduct, PremiumCalculationRequestDTO, InsurancePolicyApplicationDTO } from '../../shared/models/insurance.models';
import { AuthService } from '../../../../core/services/auth.service';
import { PremiumPaymentService } from '../../shared/services/premium-payment.service';

@Component({
  selector: 'app-client-policy-application',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './client-policy-application.component.html',
  styleUrls: ['./client-policy-application.component.css']
})
export class ClientPolicyApplicationComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly actuarialService = inject(ActuarialService);
  private readonly integratedPolicyService = inject(IntegratedPolicyService);
  private readonly productService = inject(InsuranceProductService);
  private readonly authService = inject(AuthService);
  private readonly paymentService = inject(PremiumPaymentService);

  productId!: number;
  product?: InsuranceProduct;
  
  applicationForm!: FormGroup;
  
  quote?: InsuranceCompleteQuoteDTO;
  isCalculating = false;
  isSubmitting = false;
  isPaying = false;
  applicationSuccess = false;

  ngOnInit() {
    this.productId = Number(this.route.snapshot.paramMap.get('id'));
    this.initForm();
    if (this.productId) {
      this.productService.getById(this.productId).subscribe(p => {
        this.product = p;
        this.applicationForm.patchValue({ 
          desiredCoverage: p.coverageLimit || 100000,
          durationMonths: p.durationMonths || 12
        });
      });
      this.applicationForm.patchValue({ productId: this.productId });
    }
  }

  initForm() {
    this.applicationForm = this.fb.group({
      productId: ['', Validators.required],
      desiredCoverage: [null, Validators.required],
      durationMonths: [null, Validators.required],
      paymentFrequency: ['MONTHLY', Validators.required],
      
      // Risk Profile Nested Form
      riskProfile: this.fb.group({
        age: [30, [Validators.required, Validators.min(18), Validators.max(100)]],
        monthlyIncome: [5000, [Validators.required, Validators.min(0)]],
        healthStatus: ['GOOD', Validators.required],
        hasChronicIllness: [false],
        occupationType: ['SHOP', Validators.required],
        locationRiskLevel: ['LOW', Validators.required],
        isSmoker: [false],
        dependents: [0, [Validators.required, Validators.min(0)]]
      })
    });
  }

  calculateQuote() {
    if (this.applicationForm.invalid) {
      this.applicationForm.markAllAsTouched();
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    this.isCalculating = true;
    const formVal = this.applicationForm.value;

    const request: PremiumCalculationRequestDTO = {
      insuranceType: this.product?.policyType || 'GENERAL',
      coverageAmount: formVal.desiredCoverage,
      durationMonths: formVal.durationMonths,
      paymentFrequency: formVal.paymentFrequency,
      riskProfile: formVal.riskProfile
    };

    this.actuarialService.getCompleteQuote(request).subscribe({
      next: (quote) => {
        this.quote = quote;
        this.isCalculating = false;
        setTimeout(() => {
          document.getElementById('quoteSection')?.scrollIntoView({ behavior: 'smooth' });
        }, 100);
      },
      error: (err) => {
        console.error('Failed to calculate quote', err);
        this.isCalculating = false;
        alert('Failed to calculate quote. Please try again.');
      }
    });
  }

  async submitApplication() {
    if (!this.quote) return;
    this.isSubmitting = true;

    // Ensure the session is valid before submitting
    await this.authService.ensureSessionFromApi();

    const appDTO: InsurancePolicyApplicationDTO = {
      // userId is extracted from the JWT token server-side; no need to send it
      productId: this.productId,
      desiredCoverage: this.applicationForm.value.desiredCoverage,
      durationMonths: this.applicationForm.value.durationMonths,
      paymentFrequency: this.applicationForm.value.paymentFrequency,
      riskProfile: this.applicationForm.value.riskProfile
    };

    this.integratedPolicyService.applyForInsurance(appDTO).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.applicationSuccess = true;
      },
      error: (err) => {
        console.error('Failed to submit application', err);
        this.isSubmitting = false;
        const msg = err.error?.message ?? err.error ?? err.message ?? 'Submission failed.';
        alert('Error: ' + msg);
      }
    });
  }

  payNowWithStripe() {
    if (!this.quote || !this.product) return;
    this.isPaying = true;

    const periodicPayment = this.quote.premiumDetails?.periodicPayment || 0;
    const paymentData = {
      amount: Math.round(periodicPayment * 100), // Stripe expects cents
      currency: 'usd', // Adjust currency if needed
      productName: `Initial Premium: ${this.product.productName}`,
      successUrl: `${window.location.origin}/dashboard/insurance/client/my-policies?payment=success`,
      cancelUrl: `${window.location.origin}${this.router.url}?payment=cancel`
    };

    this.paymentService.createStripeSession(paymentData).subscribe({
      next: (res) => {
        if (res.sessionUrl) {
          window.location.href = res.sessionUrl;
        }
      },
      error: (err) => {
        console.error('Stripe session creation failed', err);
        this.isPaying = false;
        alert('Failed to initialize payment. Please try again.');
      }
    });
  }

  goToMyPolicies() {
    this.router.navigate(['/dashboard/insurance/client/my-policies']);
  }
}
