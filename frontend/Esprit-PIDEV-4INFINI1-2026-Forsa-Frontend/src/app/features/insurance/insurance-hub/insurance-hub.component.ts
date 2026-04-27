import { Component, inject, signal, effect } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { PremiumPaymentService } from '../shared/services/premium-payment.service';
import { PremiumPayment } from '../shared/models/insurance.models';
import { PaymentStatus } from '../shared/enums/insurance.enums';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-insurance-hub',
  standalone: true,
  imports: [RouterLink, ForsaCardComponent, ForsaIconComponent, CommonModule],
  templateUrl: './insurance-hub.component.html',
  styleUrl: './insurance-hub.component.css',
})
export class InsuranceHubComponent {
  readonly auth = inject(AuthService);
  private readonly paymentService = inject(PremiumPaymentService);

  readonly PaymentStatus = PaymentStatus;
  upcomingReminder = signal<PremiumPayment | null>(null);

  constructor() {
    effect(() => {
      // Accessing auth.currentUser() inside effect makes this reactive
      if (this.auth.currentUser()) {
        this.checkReminders();
      }
    });
  }



  private checkReminders(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    if (!roles.includes('ROLE_CLIENT')) return;

    this.paymentService.getMyPayments().subscribe({
      next: (payments) => {
        // Find overdue first, then pending due soon
        const overdue = payments.find(p => p.status === PaymentStatus.OVERDUE);
        if (overdue) {
          this.upcomingReminder.set(overdue);
          return;
        }

        const soon = payments
          .filter(p => p.status === PaymentStatus.PENDING && !!p.dueDate)
          .sort((a, b) => new Date(a.dueDate!).getTime() - new Date(b.dueDate!).getTime())[0];
        
        if (soon && soon.dueDate) {
          // Only show if due in next 7 days
          const diffDays = Math.ceil((new Date(soon.dueDate).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24));
          if (diffDays <= 7) {
            this.upcomingReminder.set(soon);
          }
        }
      }
    });
  }

  closeReminder(): void {
    this.upcomingReminder.set(null);
  }

  get isAdminOrAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_AGENT');
  }

  get isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_CLIENT');
  }

  get isAdmin(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN');
  }

  readonly modules = [
    {
      title: 'Insurance Products',
      description: 'Catalogue of accessible health, life, and crop coverage.',
      icon: 'shield',
      route: 'products',
      tone: 'blue',
      stats: 'Manage Catalogue',
    },
    {
      title: 'Policies',
      description: 'Review and approve active coverage across the platform.',
      icon: 'layout-dashboard',
      route: 'policies',
      tone: 'emerald',
      stats: 'Policy Management',
    },
    {
      title: 'Insurance Claims',
      description: 'Process indemnification requests and track status.',
      icon: 'alert-circle',
      route: 'claims',
      tone: 'amber',
      stats: 'Claim Processing',
    },
    {
      title: 'Premium Payments',
      description: 'Monitor periodic payments for all active policies.',
      icon: 'credit-card',
      route: 'payments',
      tone: 'rose',
      stats: 'Billing Terminal',
    },
  ];
}
