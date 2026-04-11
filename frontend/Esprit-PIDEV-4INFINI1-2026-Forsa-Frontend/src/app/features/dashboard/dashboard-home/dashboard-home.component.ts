import { DecimalPipe } from '@angular/common';
import { Component } from '@angular/core';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { MiniAreaChartComponent } from '../../../shared/charts/mini-area-chart.component';
import { MOCK_TRANSACTIONS } from '../../../core/data/mock-data';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [DecimalPipe, ForsaCardComponent, ForsaIconComponent, MiniAreaChartComponent],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.css',
})
export class DashboardHomeComponent {
  readonly transactions = MOCK_TRANSACTIONS;
  readonly chartValues = [4000, 3000, 2000, 2780, 1890, 2390, 3490];
  readonly stats = [
    { label: 'Total Credits', value: '$1.2M', change: '+12.5%', icon: 'credit-card' as const, tone: 'blue' },
    { label: 'Active Users', value: '2,450', change: '+18.2%', icon: 'users' as const, tone: 'blue' },
    { label: 'Wallet Volume', value: '$840K', change: '+5.4%', icon: 'wallet' as const, tone: 'blue' },
    { label: 'Insurance Policies', value: '1,120', change: '+2.1%', icon: 'shield-check' as const, tone: 'blue' },
  ];
}
