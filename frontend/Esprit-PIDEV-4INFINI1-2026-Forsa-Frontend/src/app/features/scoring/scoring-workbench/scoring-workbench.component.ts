import { DecimalPipe, PercentPipe } from '@angular/common';
import { Component, computed, signal } from '@angular/core';
import { MOCK_SCORING_CLIENTS } from '../../../core/data/mock-data';
import type { RiskCategory } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

@Component({
  selector: 'app-scoring-workbench',
  standalone: true,
  imports: [
    DecimalPipe,
    PercentPipe,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
  ],
  templateUrl: './scoring-workbench.component.html',
  styleUrl: './scoring-workbench.component.css',
})
export class ScoringWorkbenchComponent {
  readonly demos = MOCK_SCORING_CLIENTS;

  readonly selectedClientId = signal<number>(MOCK_SCORING_CLIENTS[0].clientId);

  readonly selected = computed(() => {
    const id = this.selectedClientId();
    return this.demos.find((d) => d.clientId === id) ?? this.demos[0];
  });

  readonly factorRows = computed(() => {
    const s = this.selected().latestScore;
    return [
      { label: 'Income stability', raw: s.factor1Score, weight: s.factor1Contribution },
      { label: 'Payment history', raw: s.factor2Score, weight: s.factor2Contribution },
      { label: 'Debt ratio', raw: s.factor3Score, weight: s.factor3Contribution },
      { label: 'Employment type', raw: s.factor4Score, weight: s.factor4Contribution },
      { label: 'Region', raw: s.factor5Score, weight: s.factor5Contribution },
    ];
  });

  pickClient(id: number): void {
    this.selectedClientId.set(id);
  }

  categoryTone(cat: RiskCategory): 'success' | 'info' | 'warning' | 'danger' | 'muted' {
    switch (cat) {
      case 'EXCELLENT':
        return 'success';
      case 'GOOD':
        return 'info';
      case 'MODERATE':
        return 'warning';
      case 'RISKY':
      case 'VERY_RISKY':
        return 'danger';
    }
  }

  barClass(score: number): string {
    if (score >= 70) return 'factor-bar__fill--ok';
    if (score >= 50) return 'factor-bar__fill--mid';
    return 'factor-bar__fill--bad';
  }
}
