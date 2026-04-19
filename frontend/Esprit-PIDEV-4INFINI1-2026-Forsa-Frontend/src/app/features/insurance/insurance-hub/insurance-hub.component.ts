import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

interface InsuranceModule {
  title: string;
  description: string;
  icon: string;
  route: string;
  tone: string;
  stats: string;
}

@Component({
  selector: 'app-insurance-hub',
  standalone: true,
  imports: [RouterLink, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './insurance-hub.component.html',
  styleUrl: './insurance-hub.component.css',
})
export class InsuranceHubComponent {
  readonly modules: InsuranceModule[] = [
    {
      title: 'Insurance Products',
      description: 'Create and manage insurance product catalogue — health, life, crop and more.',
      icon: 'shield',
      route: 'products',
      tone: 'blue',
      stats: 'Manage Products',
    },
    {
      title: 'Policies',
      description: 'Review policy applications, approve or suspend active policies.',
      icon: 'layout-dashboard',
      route: 'policies',
      tone: 'emerald',
      stats: 'Manage Policies',
    },
    {
      title: 'Insurance Claims',
      description: 'Track and process client claims, update status and indemnification.',
      icon: 'alert-circle',
      route: 'claims',
      tone: 'amber',
      stats: 'Manage Claims',
    },
    {
      title: 'Premium Payments',
      description: 'Monitor periodic premium payments, mark as paid or late.',
      icon: 'credit-card',
      route: 'payments',
      tone: 'rose',
      stats: 'Manage Payments',
    },
  ];
}
