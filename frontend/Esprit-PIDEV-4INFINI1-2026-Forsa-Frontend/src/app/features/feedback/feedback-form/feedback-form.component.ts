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
  imports: [
    CommonModule,
    FormsModule,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
  ],
  templateUrl: './feedback-form.component.html',
  styleUrl: './feedback-form.component.css',
})
export class FeedbackFormComponent implements OnInit {
  feedback: Feedback = {
    rating: 3,
    comment: '',
    satisfactionLevel: 'NEUTRAL',
    isAnonymous: false,
  };

  isEditMode = false;
  ratings = [1, 2, 3, 4, 5];
  useAI = false;
  loading = false;
  error = '';
  complaintId?: number;

  satisfactionLevels: SatisfactionLevel[] = [
    'VERY_SATISFIED',
    'SATISFIED',
    'NEUTRAL',
    'DISSATISFIED',
    'VERY_DISSATISFIED',
  ];

  constructor(
    private feedbackService: FeedbackService,
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    if (!this.isClient) {
      this.router.navigate(['/dashboard/feedback']);
      return;
    }
    const complaintId = this.route.snapshot.queryParamMap.get('complaintId');
    if (complaintId) {
      this.complaintId = +complaintId;
      this.feedback.complaint = { id: +complaintId, subject: '', description: '' };
    }
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loading = true;
      this.feedbackService.getById(+id).subscribe({
        next: (data: Feedback) => {
          this.feedback = data;
          this.loading = false;
        },
        error: () => {
          this.error = 'Error loading feedback';
          this.loading = false;
        },
      });
    }
  }

  get isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_CLIENT') || roles.includes('CLIENT');
  }

  setRating(rating: number): void {
    this.feedback.rating = rating;
  }

  getRatingStars(rating: number): number[] {
    return Array.from({ length: 5 }, (_, i) => i + 1);
  }

  getStarClass(star: number): string {
    if (!this.feedback.rating) return '';
    return star <= this.feedback.rating ? 'star-filled' : 'star-empty';
  }

  save(): void {
    if (!this.feedback.rating || this.feedback.rating < 1 || this.feedback.rating > 5) {
      this.error = 'Rating must be between 1 and 5.';
      return;
    }
    if ((this.feedback.comment ?? '').length > 500) {
      this.error = 'Comment must be 500 characters max.';
      return;
    }
    this.loading = true;
    this.error = '';
    const payload: any = {
      ...this.feedback,
      complaint: this.feedback.complaint ?? (this.complaintId ? { id: this.complaintId, subject: '', description: '' } : undefined),
    };
    console.log('[FeedbackForm] submit payload:', payload);

    if (this.isEditMode) {
      const updatePayload: any = {
        ...this.feedback,
        id: this.feedback.id,
        complaint: this.feedback.complaint ??
          (this.complaintId ? { id: this.complaintId, subject: '', description: '' } : undefined),
      };
      this.feedbackService.update(updatePayload).subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Error updating feedback';
          this.loading = false;
        },
      });
    } else {
      const action = this.useAI
        ? this.feedbackService.addWithAI(payload)
        : this.feedbackService.add(payload);

      action.subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Error submitting feedback';
          this.loading = false;
        },
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/dashboard/feedback']);
  }

  remove(): void {
    if (!this.isEditMode || !this.feedback.id) {
      return;
    }
    this.feedbackService.delete(this.feedback.id).subscribe({
      next: () => this.router.navigate(['/dashboard/feedback']),
      error: () => (this.error = 'Error deleting feedback'),
    });
  }
}
