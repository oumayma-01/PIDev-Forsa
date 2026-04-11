import { DecimalPipe } from '@angular/common';
import { Component } from '@angular/core';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { MOCK_CREDITS } from '../../../core/data/mock-data';
import type { CreditRequest } from '../../../core/models/forsa.models';

@Component({
  selector: 'app-credit-list',
  standalone: true,
  imports: [
    DecimalPipe,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
  ],
  templateUrl: './credit-list.component.html',
  styleUrl: './credit-list.component.css',
})
export class CreditListComponent {
  readonly credits = MOCK_CREDITS;

  riskClass(score: number): string {
    if (score > 80) return 'risk-bar__fill--ok';
    if (score > 50) return 'risk-bar__fill--mid';
    return 'risk-bar__fill--bad';
  }

  statusTone(status: CreditRequest['status']): 'success' | 'warning' | 'danger' | 'info' {
    switch (status) {
      case 'approved':
        return 'success';
      case 'pending':
        return 'warning';
      case 'rejected':
        return 'danger';
      case 'disbursed':
        return 'info';
    }
  }
}
