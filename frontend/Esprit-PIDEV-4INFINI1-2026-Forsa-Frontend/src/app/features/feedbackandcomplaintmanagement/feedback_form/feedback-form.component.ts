import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { FeedbackService } from '../../../core/data/feedback.service';
import { Feedback, SatisfactionLevel } from '../../../core/models/forsa.models';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';

@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [CommonModule, FormsModule, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './feedback-form.component.html',
  styleUrl: './feedback-form.component.css',
})
export class FeedbackFormComponent implements OnInit {

  feedback: Feedback = {
    rating: 3,
    comment: '',
    satisfactionLevel: 'NEUTRAL',
    isAnonymous: false
  };

  isEditMode = false;
  useAI = false;
  loading = false;
  error = '';

  ratings = [1, 2, 3, 4, 5];
  satisfactionLevels: SatisfactionLevel[] = [
    'VERY_SATISFIED',
    'SATISFIED',
    'NEUTRAL',
    'DISSATISFIED',
    'VERY_DISSATISFIED'
  ];

  constructor(
    private feedbackService: FeedbackService,
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.feedbackService.getById(+id).subscribe({
        next: (data: Feedback) => this.feedback = data,
        error: () => this.error = 'Erreur lors du chargement'
      });
    }
  }

  get isClient(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_CLIENT') ?? false;
  }

  setRating(star: number): void {
    this.feedback.rating = star;
  }

  save(): void {
    this.loading = true;
    this.error = '';

    if (this.isEditMode) {
      this.feedbackService.update(this.feedback).subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Erreur lors de la modification';
          this.loading = false;
        }
      });
    } else {
      const action = this.useAI
        ? this.feedbackService.addWithAI(this.feedback)
        : this.feedbackService.add(this.feedback);

      action.subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Erreur lors de l\'ajout';
          this.loading = false;
        }
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/dashboard/feedback']);
  }
}
