import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { InsurancePolicy } from '../../../shared/models/insurance.models';
import { PolicyStatus } from '../../../shared/enums/insurance.enums';

@Component({
  selector: 'app-policy-edit',
  standalone: true,
  imports: [RouterLink, FormsModule, CommonModule, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  template: `
    <div class="page-head">
      <div>
        <h2>Edit Insurance Policy</h2>
        <p>Modify policy parameters and details.</p>
      </div>
      <a [routerLink]="['/dashboard/insurance/policies', policyId]">
        <app-forsa-button variant="outline">Cancel</app-forsa-button>
      </a>
    </div>

    @if (loading()) {
      <div class="state-box"><app-forsa-icon name="history" [size]="32" class="spin" /></div>
    } @else if (error()) {
      <div class="state-box state-error">
        <app-forsa-icon name="alert-circle" [size]="28" /><p>{{ error() }}</p>
      </div>
    } @else {
      <div class="form-container">
        <app-forsa-card>
          <div class="card-pad">
            <form (ngSubmit)="submit()" #policyForm="ngForm">
              <div class="form-grid">
                <!-- Basic Info -->
                <div class="form-group">
                  <label>Policy Number</label>
                  <input type="text" [(ngModel)]="form.policyNumber" name="policyNumber" class="forsa-input" readonly>
                </div>

                <div class="form-group">
                  <label>Status</label>
                  <select [(ngModel)]="form.status" name="status" class="forsa-input">
                    <option [value]="Status.PENDING">Pending</option>
                    <option [value]="Status.ACTIVE">Active</option>
                    <option [value]="Status.SUSPENDED">Suspended</option>
                    <option [value]="Status.CANCELLED">Cancelled</option>
                    <option [value]="Status.EXPIRED">Expired</option>
                  </select>
                </div>

                <div class="form-group">
                  <label>Coverage Limit (TND)</label>
                  <input type="number" [(ngModel)]="form.coverageLimit" name="coverageLimit" class="forsa-input" required>
                </div>

                <div class="form-group">
                  <label>Final Premium (TND)</label>
                  <input type="number" [(ngModel)]="form.finalPremium" name="finalPremium" class="forsa-input" required>
                </div>

                <div class="form-group">
                  <label>Start Date</label>
                  <input type="date" [(ngModel)]="form.startDate" name="startDate" class="forsa-input">
                </div>

                <div class="form-group">
                  <label>End Date</label>
                  <input type="date" [(ngModel)]="form.endDate" name="endDate" class="forsa-input">
                </div>

                <div class="form-group">
                  <label>Next Due Date</label>
                  <input type="date" [(ngModel)]="form.nextPremiumDueDate" name="nextPremiumDueDate" class="forsa-input">
                </div>

                <div class="form-group">
                    <label>Payment Frequency</label>
                    <input type="text" [(ngModel)]="form.paymentFrequency" name="paymentFrequency" class="forsa-input">
                </div>
              </div>

              <div class="form-group full-width">
                <label>Calculation & Decision Notes</label>
                <textarea [(ngModel)]="form.calculationNotes" name="calculationNotes" class="forsa-textarea" rows="4"></textarea>
              </div>

              <div class="form-actions">
                <app-forsa-button type="submit" [disabled]="saving() || !policyForm.form.valid">
                  @if (saving()) { <app-forsa-icon name="history" [size]="16" class="spin" /> }
                  Save Changes
                </app-forsa-button>
              </div>
            </form>
          </div>
        </app-forsa-card>
      </div>
    }
  `,
  styles: [`
    .form-container { max-width: 800px; margin: 0 auto; }
    .card-pad { padding: 2rem; }
    .form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; margin-bottom: 1.5rem; }
    .form-group { display: flex; flex-direction: column; gap: 0.5rem; }
    .full-width { grid-column: span 2; margin-top: 1rem; }
    .form-group label { font-size: 0.85rem; font-weight: 600; color: var(--color-muted-foreground); }
    .forsa-input, .forsa-textarea { 
      padding: 0.75rem; border-radius: 0.5rem; 
      border: 1px solid var(--color-border);
      background-color: var(--color-background);
      color: var(--color-foreground);
      font-size: 0.95rem; transition: all 0.2s; 
    }
    .forsa-input:focus, .forsa-textarea:focus { outline: none; border-color: var(--color-primary); box-shadow: 0 0 0 2px rgba(30, 64, 175, 0.1); }
    .forsa-input[readonly] { background: var(--color-muted); opacity: 0.7; cursor: not-allowed; }
    .form-actions { margin-top: 2rem; display: flex; justify-content: flex-end; }
    .state-box { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 5rem; color: var(--color-muted-foreground); }
    .spin { animation: spin 1s linear infinite; }
    @keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
  `]
})
export class PolicyEditComponent implements OnInit {
  private readonly svc = inject(InsurancePolicyService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly Status = PolicyStatus;
  policyId!: number;
  loading = signal(true);
  saving = signal(false);
  error = signal<string | null>(null);

  form: Partial<InsurancePolicy> = {};

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('No policy ID provided');
      this.loading.set(false);
      return;
    }
    this.policyId = +id;
    this.load();
  }

  load(): void {
    this.svc.getById(this.policyId).subscribe({
      next: (p) => {
        // Convert dates for input[type="date"]
        this.form = {
          ...p,
          startDate: p.startDate ? new Date(p.startDate).toISOString().split('T')[0] : '',
          endDate: p.endDate ? new Date(p.endDate).toISOString().split('T')[0] : '',
          nextPremiumDueDate: p.nextPremiumDueDate ? new Date(p.nextPremiumDueDate).toISOString().split('T')[0] : ''
        };
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(e.message ?? 'Failed to load policy');
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    this.saving.set(true);
    this.svc.update(this.form as InsurancePolicy).subscribe({
      next: () => {
        this.saving.set(false);
        alert('Policy updated successfully');
        this.router.navigate(['/dashboard/insurance/policies', this.policyId]);
      },
      error: (e) => {
        alert('Failed to update policy: ' + (e.error?.message ?? e.message));
        this.saving.set(false);
      }
    });
  }
}
