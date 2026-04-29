import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe, DecimalPipe } from '@angular/common';
import { ForsaBadgeComponent } from '../../../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../../../shared/ui/forsa-icon/forsa-icon.component';
import { InsurancePolicyService } from '../../../shared/services/insurance-policy.service';
import { InsurancePolicy } from '../../../shared/models/insurance.models';
import { PolicyStatus } from '../../../shared/enums/insurance.enums';
import { AuthService } from '../../../../../core/services/auth.service';
import { DigitalSignatureComponent } from '../../../../../shared/ui/forsa-signature/forsa-signature.component';

@Component({
  selector: 'app-policy-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, DigitalSignatureComponent],
  templateUrl: './policy-detail.component.html',
  styleUrl: './policy-detail.component.css',
})
export class PolicyDetailComponent implements OnInit {
  private readonly svc = inject(InsurancePolicyService);
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);

  policy = signal<InsurancePolicy | null>(null);
  loading = signal(true);
  error = signal<string | null>(null);
  downloadingAmortization = signal(false);
  downloadingContract = signal(false);
  showSignatureUI = signal(false);
  signing = signal(false);

  get isAdmin(): boolean {
    return this.auth.currentUser()?.roles.includes('ROLE_ADMIN') || false;
  }

  get isAgent(): boolean {
    return this.auth.currentUser()?.roles.includes('ROLE_AGENT') || false;
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.error.set('No ID provided'); this.loading.set(false); return; }
    this.svc.getById(+id).subscribe({
      next: (p) => { this.policy.set(p); this.loading.set(false); },
      error: (e) => { this.error.set(e.message ?? 'Failed to load.'); this.loading.set(false); },
    });
  }

  downloadAmortization(): void {
    const p = this.policy();
    if (!p?.id) return;
    this.downloadingAmortization.set(true);
    this.svc.downloadAmortizationPdf(p.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `Amortization_Schedule_${p.policyNumber}.pdf`; a.click();
        URL.revokeObjectURL(url);
        this.downloadingAmortization.set(false);
      },
      error: () => { alert('Amortization download failed.'); this.downloadingAmortization.set(false); },
    });
  }

  viewAmortization(): void {
    const p = this.policy();
    if (!p?.id) return;
    this.svc.viewAmortizationPdf(p.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: () => { alert('Amortization view failed.'); },
    });
  }

  downloadContract(): void {
    const p = this.policy();
    if (!p?.id) return;
    this.downloadingContract.set(true);
    this.svc.downloadPolicyContract(p.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = `Contract_${p.policyNumber}.pdf`; a.click();
        URL.revokeObjectURL(url);
        this.downloadingContract.set(false);
      },
      error: () => { alert('Contract download failed.'); this.downloadingContract.set(false); },
    });
  }

  viewContract(): void {
    const p = this.policy();
    if (!p?.id) return;
    this.svc.viewPolicyContract(p.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
      },
      error: () => { alert('Contract view failed.'); },
    });
  }

  handleSign(signature: string): void {
    const p = this.policy();
    if (!p?.id) return;
    this.signing.set(true);
    this.svc.signPolicy(p.id, signature).subscribe({
      next: (updated) => {
        this.policy.set(updated);
        this.signing.set(false);
        this.showSignatureUI.set(false);
        alert('Contract signed successfully!');
      },
      error: (e) => {
        alert('Signing failed: ' + (e.error?.message ?? e.message));
        this.signing.set(false);
      }
    });
  }

  adminAutoSign(): void {
    const p = this.policy();
    if (!p?.id) return;
    
    // Create a digital stamp image using a temporary canvas
    const canvas = document.createElement('canvas');
    canvas.width = 300;
    canvas.height = 150;
    const ctx = canvas.getContext('2d')!;
    
    // Stamp background (circle/oval)
    ctx.strokeStyle = '#003399';
    ctx.lineWidth = 4;
    ctx.beginPath();
    ctx.ellipse(150, 75, 140, 70, 0, 0, Math.PI * 2);
    ctx.stroke();
    
    // Company name
    ctx.font = 'bold 20px Arial';
    ctx.fillStyle = '#003399';
    ctx.textAlign = 'center';
    ctx.fillText('FORSA INSURANCE', 150, 55);
    
    // Signer info
    ctx.font = '14px Arial';
    ctx.fillText('OFFICIALLY STAMPED', 150, 80);
    ctx.fillText(this.auth.currentUser()?.username.toUpperCase() ?? 'ADMIN', 150, 100);
    
    // Date
    ctx.font = 'italic 12px Arial';
    ctx.fillText(new Date().toLocaleDateString(), 150, 125);
    
    this.handleSign(canvas.toDataURL('image/png'));
  }

  statusTone(s?: PolicyStatus): 'success' | 'warning' | 'danger' | 'info' | 'muted' {
    switch (s) {
      case PolicyStatus.ACTIVE: return 'success';
      case PolicyStatus.PENDING: return 'warning';
      case PolicyStatus.SUSPENDED: return 'info';
      case PolicyStatus.CANCELLED:
      case PolicyStatus.EXPIRED: return 'danger';
      default: return 'muted';
    }
  }
}
