import { DecimalPipe, TitleCasePipe } from '@angular/common';
import { Component } from '@angular/core';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { MOCK_POLICIES } from '../../../core/data/mock-data';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';
import type { InsurancePolicy } from '../../../core/models/forsa.models';

@Component({
  selector: 'app-insurance-products',
  standalone: true,
  imports: [DecimalPipe, TitleCasePipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './insurance-products.component.html',
  styleUrl: './insurance-products.component.css',
})
export class InsuranceProductsComponent {
  readonly policies = MOCK_POLICIES;
  readonly products: { id: string; name: string; icon: ForsaIconName; description: string; tone: string }[] = [
    { id: '1', name: 'Life Insurance', icon: 'heart', description: "Protect your family's future with comprehensive life coverage.", tone: 'rose' },
    { id: '2', name: 'Health Plus', icon: 'shield-check', description: 'Premium health coverage including dental and vision care.', tone: 'emerald' },
    { id: '3', name: 'Auto Guard', icon: 'car', description: 'Full protection for your vehicle against accidents and theft.', tone: 'blue' },
    { id: '4', name: 'Home Secure', icon: 'home', description: 'Safeguard your home and belongings from unexpected events.', tone: 'amber' },
  ];

  policyIcon(type: InsurancePolicy['type']): ForsaIconName {
    switch (type) {
      case 'health':
        return 'heart';
      case 'auto':
        return 'car';
      case 'life':
        return 'shield-check';
    }
  }
}
