import { Component } from '@angular/core';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { MiniLineChartComponent } from '../../../shared/charts/mini-line-chart.component';
import { MiniRadarChartComponent, type RadarDatum } from '../../../shared/charts/mini-radar-chart.component';

@Component({
  selector: 'app-risk-analysis',
  standalone: true,
  imports: [ForsaBadgeComponent, ForsaCardComponent, ForsaIconComponent, MiniLineChartComponent, MiniRadarChartComponent],
  templateUrl: './risk-analysis.component.html',
  styleUrl: './risk-analysis.component.css',
})
export class RiskAnalysisComponent {
  readonly radarData: RadarDatum[] = [
    { label: 'Credit History', value: 120, max: 150 },
    { label: 'Income Stability', value: 98, max: 150 },
    { label: 'Debt-to-Income', value: 86, max: 150 },
    { label: 'Asset Value', value: 99, max: 150 },
    { label: 'Payment Behavior', value: 85, max: 150 },
    { label: 'Market Risk', value: 65, max: 150 },
  ];

  readonly trendPoints = [
    { x: 'Oct', y: 65 },
    { x: 'Nov', y: 68 },
    { x: 'Dec', y: 72 },
    { x: 'Jan', y: 70 },
    { x: 'Feb', y: 75 },
    { x: 'Mar', y: 82 },
  ];
}
