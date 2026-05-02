import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { RouterModule, RouterLink } from '@angular/router';
import { InsurancePolicyService } from '../../shared/services/insurance-policy.service';
import { InsurancePolicy } from '../../shared/models/insurance.models';
import { PolicyChatComponent } from './policy-chat/policy-chat.component';
import { DigitalSignatureComponent } from '../../../../shared/ui/forsa-signature/forsa-signature.component';
import { ForsaIconComponent } from '../../../../shared/ui/forsa-icon/forsa-icon.component';

@Component({
  selector: 'app-client-my-policies',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, PolicyChatComponent, DigitalSignatureComponent, ForsaIconComponent, CommonModule],
  templateUrl: './client-my-policies.component.html',
  styleUrls: ['./client-my-policies.component.css']
})
export class ClientMyPoliciesComponent implements OnInit {
  private readonly policyService = inject(InsurancePolicyService);

  policies: InsurancePolicy[] = [];
  isLoading = true;
  selectedPolicyIdForChat: number | null = null;
  
  showSignatureUI = false;
  selectedPolicyForSigning: InsurancePolicy | null = null;
  isSigning = false;

  ngOnInit() {
    this.policyService.getMyPolicies().subscribe({
      next: (data) => {
        this.policies = data;
        this.isLoading = false;
        if (this.policies.length > 0 && !this.selectedPolicyIdForChat) {
          this.selectedPolicyIdForChat = this.policies[0].id!;
        }
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  downloadAmortization(policy: InsurancePolicy) {
    if (!policy.id) return;
    this.policyService.downloadAmortizationPdf(policy.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Amortization_${policy.policyNumber || policy.id}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  viewAmortization(policy: InsurancePolicy) {
    if (!policy.id) return;
    this.policyService.viewAmortizationPdf(policy.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
    });
  }

  downloadContract(policy: InsurancePolicy) {
    if (!policy.id) return;
    this.policyService.downloadPolicyContract(policy.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Insurance_Contract_${policy.policyNumber || policy.id}.pdf`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  viewContract(policy: InsurancePolicy) {
    if (!policy.id) return;
    this.policyService.viewPolicyContract(policy.id).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
    });
  }

  openChat(policyId: number) {
    this.selectedPolicyIdForChat = policyId;
  }

  openSignModal(policy: InsurancePolicy) {
    this.selectedPolicyForSigning = policy;
    this.showSignatureUI = true;
  }

  handleSign(signature: string) {
    if (!this.selectedPolicyForSigning?.id) return;
    this.isSigning = true;
    this.policyService.signPolicy(this.selectedPolicyForSigning.id, signature).subscribe({
      next: (updated) => {
        const idx = this.policies.findIndex(p => p.id === updated.id);
        if (idx !== -1) this.policies[idx] = updated;
        this.isSigning = false;
        this.showSignatureUI = false;
        this.selectedPolicyForSigning = null;
      },
      error: (err) => {
        console.error(err);
        this.isSigning = false;
        alert('Signing failed. Please try again.');
      }
    });
  }
}
