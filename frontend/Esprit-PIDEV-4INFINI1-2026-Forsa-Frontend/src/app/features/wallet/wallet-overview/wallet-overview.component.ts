import { DecimalPipe } from '@angular/common';
import { Component } from '@angular/core';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { MOCK_TRANSACTIONS } from '../../../core/data/mock-data';

@Component({
  selector: 'app-wallet-overview',
  standalone: true,
  imports: [DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './wallet-overview.component.html',
  styleUrl: './wallet-overview.component.css',
})
export class WalletOverviewComponent {
  readonly transactions = MOCK_TRANSACTIONS;
}
