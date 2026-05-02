import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FeedbackFacadeService } from '../feedback-facade.service';
import { Feedback } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-feedbacks-view',
  standalone: true,
  imports: [CommonModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './feedbacks-view.component.html',
})
export class FeedbacksViewComponent implements OnInit {
  private readonly facade = inject(FeedbackFacadeService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);
  items: Feedback[] = [];
  loading = false;
  error = '';
  isAdmin = false;
  isAgent = false;

  ngOnInit(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    this.isAdmin = roles.some((role) => role === 'ADMIN' || role === 'ROLE_ADMIN' || role.replace('ROLE_', '') === 'ADMIN');
    this.isAgent = roles.some((role) => role === 'AGENT' || role === 'ROLE_AGENT' || role.replace('ROLE_', '') === 'AGENT');
    this.loading = true;
    this.facade.getAllFeedbacks().subscribe({
      next: (d) => { this.items = d ?? []; this.loading = false; },
      error: () => { this.error = 'Error loading feedbacks'; this.loading = false; },
    });
  }
  goBack(): void { this.router.navigate(['/dashboard/feedback']); }

  deleteFeedback(id: number): void {
    if (!confirm('Delete this feedback?')) return;
    this.facade.deleteFeedback(id).subscribe({
      next: () => { this.items = this.items.filter((x) => x.id !== id); },
      error: () => { this.error = 'Unable to delete feedback'; },
    });
  }

  feedbackAuthor(item: Feedback): string {
    const source: any = item as any;
    const user = source?.user ?? source?.client ?? source?.author ?? source?.createdBy;
    const fullName = user?.fullName ?? user?.name;
    const username = user?.username ?? user?.login;
    const email = user?.email;
    const id = user?.id ?? source?.clientId;
    return fullName || username || email || (id ? `User #${id}` : 'Unknown user');
  }
}
