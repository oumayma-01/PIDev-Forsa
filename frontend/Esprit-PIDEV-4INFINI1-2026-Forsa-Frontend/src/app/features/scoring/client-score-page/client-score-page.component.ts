import { Component } from '@angular/core';
import { ScoreClientWidgetComponent } from '../score-client-widget/score-client-widget.component';

@Component({
  selector: 'app-client-score-page',
  standalone: true,
  imports: [ScoreClientWidgetComponent],
  templateUrl: './client-score-page.component.html',
  styleUrl: './client-score-page.component.css',
})
export class ClientScorePageComponent {}
