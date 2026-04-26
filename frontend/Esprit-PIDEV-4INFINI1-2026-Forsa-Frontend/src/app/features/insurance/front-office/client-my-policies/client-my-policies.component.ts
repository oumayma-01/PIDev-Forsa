import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { InsurancePolicyService } from '../../shared/services/insurance-policy.service';
import { InsurancePolicy } from '../../shared/models/insurance.models';
import { PolicyChatComponent } from './policy-chat/policy-chat.component';

@Component({
  selector: 'app-client-my-policies',
  standalone: true,
  imports: [CommonModule, RouterModule, PolicyChatComponent],
  templateUrl: './client-my-policies.component.html',
  styleUrls: ['./client-my-policies.component.css']
})
export class ClientMyPoliciesComponent implements OnInit {
  private readonly policyService = inject(InsurancePolicyService);

  policies: InsurancePolicy[] = [];
  isLoading = true;
  selectedPolicyIdForChat: number | null = null;

  ngOnInit() {
    this.policyService.getMyPolicies().subscribe({
      next: (data) => {
        this.policies = data;
        this.isLoading = false;
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

  openChat(policyId: number) {
    this.selectedPolicyIdForChat = policyId;
  }
}
