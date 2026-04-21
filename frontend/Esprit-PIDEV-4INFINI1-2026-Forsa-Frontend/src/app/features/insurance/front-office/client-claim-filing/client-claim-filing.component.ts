import { Component, OnInit, inject, signal, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { InsuranceClaimService } from '../../shared/services/insurance-claim.service';
import { InsurancePolicyService } from '../../shared/services/insurance-policy.service';
import { InsurancePolicy, InsuranceClaim } from '../../shared/models/insurance.models';
import { ForsaCardComponent } from '../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaButtonComponent } from '../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaBadgeComponent } from '../../../../shared/ui/forsa-badge/forsa-badge.component';

@Component({
  selector: 'app-client-claim-filing',
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterModule, 
    ForsaCardComponent, 
    ForsaIconComponent, 
    ForsaButtonComponent,
    ForsaBadgeComponent
  ],
  templateUrl: './client-claim-filing.component.html',
  styleUrls: ['./client-claim-filing.component.css']
})
export class ClientClaimFilingComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private claimService = inject(InsuranceClaimService);
  private policyService = inject(InsurancePolicyService);

  policy = signal<InsurancePolicy | null>(null);
  loading = signal(true);
  submitting = signal(false);
  error = signal<string | null>(null);
  success = signal(false);

  currentStep = signal(1);
  claimForm!: FormGroup;
  selectedFile: File | null = null;
  filePreview: string | null = null;

  // Car Damage Points
  damagePoints = signal<{ x: number, y: number, part: string }[]>([]);

  accidentTypes = [
    { id: 'collision', label: 'Collision / Accident', icon: 'zap' },
    { id: 'theft', label: 'Theft / Burglary', icon: 'shield' },
    { id: 'fire', label: 'Fire Damage', icon: 'zap' },
    { id: 'natural', label: 'Natural Disaster', icon: 'history' },
    { id: 'vandalism', label: 'Vandalism', icon: 'alert-circle' },
    { id: 'other', label: 'Other', icon: 'more-horizontal' }
  ];

  ngOnInit() {
    const policyId = this.route.snapshot.params['policyId'];
    if (policyId) {
      this.loadPolicy(policyId);
    }

    this.claimForm = this.fb.group({
      incidentDate: [new Date().toISOString().split('T')[0], Validators.required],
      claimAmount: ['', [Validators.required, Validators.min(1)]],
      accidentType: ['collision', Validators.required],
      description: ['', [Validators.required, Validators.minLength(20)]]
    });
  }

  loadPolicy(id: number) {
    this.policyService.getById(id).subscribe({
      next: (p: InsurancePolicy) => {
        this.policy.set(p);
        this.loading.set(false);
      },
      error: () => {
        this.error.set("Could not load policy details.");
        this.loading.set(false);
      }
    });
  }

  nextStep() {
    if (this.currentStep() < 3) {
      this.currentStep.update(s => s + 1);
    }
  }

  prevStep() {
    if (this.currentStep() > 1) {
      this.currentStep.update(s => s - 1);
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = () => this.filePreview = reader.result as string;
      reader.readAsDataURL(file);
    }
  }

  addDamagePoint(event: MouseEvent) {
    // Looser check to ensure car map shows for anything that might be a vehicle
    const pType = (this.policy()?.insuranceProduct?.policyType || '').toLowerCase();
    const pName = (this.policy()?.insuranceProduct?.productName || '').toLowerCase();
    if (!pType.includes('motor') && !pType.includes('accident') && !pName.includes('car') && !pName.includes('auto')) {
       // Optional: for testing show anyway or handle other types
    }

    const target = event.currentTarget as HTMLElement;
    const rect = target.getBoundingClientRect();
    const x = ((event.clientX - rect.left) / rect.width) * 100;
    const y = ((event.clientY - rect.top) / rect.height) * 100;
    
    let part = "Body Panel";
    if (y < 35) part = "Upper / Glass";
    if (y > 65) part = "Lower / Wheels";
    if (x < 35) part = "Front End";
    if (x > 65) part = "Rear End";

    this.damagePoints.update(pts => [...pts, { x, y, part }]);
  }

  clearPoints() {
    this.damagePoints.set([]);
  }

  async submitClaim() {
    if (this.claimForm.invalid) return;
    this.submitting.set(true);
    this.error.set(null);

    try {
      let attachmentUrl = '';
      if (this.selectedFile) {
        attachmentUrl = await firstValueFrom(this.claimService.uploadAttachment(this.selectedFile));
      }

      const claim: InsuranceClaim = {
        ...this.claimForm.value,
        claimDate: new Date().toISOString(),
        status: 'SUBMITTED' as any,
        insurancePolicy: { id: parseInt(this.policy()!.id!.toString()) },
        attachmentUrl: attachmentUrl,
        damagedPoints: JSON.stringify(this.damagePoints()),
        claimNumber: 'CLM-' + Date.now() + '-' + Math.floor(Math.random() * 1000)
      };

      console.log("Submitting claim:", claim);

      this.claimService.create(claim).subscribe({
        next: (response) => {
          console.log("Created claim response:", response);
          this.success.set(true);
          this.submitting.set(false);
        },
        error: (err: any) => {
          console.error("Filing error:", err);
          const msg = err.error?.message || "Submission failed. Please check your data or connection.";
          this.error.set(msg);
          this.submitting.set(false);
        }
      });
    } catch (err: any) {
      console.error("Upload error:", err);
      this.error.set("File upload failed. Please try again.");
      this.submitting.set(false);
    }
  }

  goToDashboard() {
    this.router.navigate(['/dashboard/insurance/client/my-claims']);
  }
}
