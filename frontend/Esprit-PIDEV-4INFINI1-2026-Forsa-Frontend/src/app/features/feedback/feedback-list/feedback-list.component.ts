import { Component } from '@angular/core';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { MOCK_COMPLAINTS } from '../../../core/data/mock-data';
import type { Complaint } from '../../../core/models/forsa.models';

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './feedback-list.component.html',
  styleUrl: './feedback-list.component.css',
})
export class FeedbackListComponent {
  readonly items = MOCK_COMPLAINTS;

  statusIcon(status: Complaint['status']): 'alert-circle' | 'clock' | 'check-circle-2' {
    switch (status) {
      case 'open':
        return 'alert-circle';
      case 'in-progress':
        return 'clock';
      case 'resolved':
        return 'check-circle-2';
    }
  }

  statusTone(status: Complaint['status']): 'info' | 'warning' | 'success' {
    switch (status) {
      case 'open':
        return 'info';
      case 'in-progress':
        return 'warning';
      case 'resolved':
        return 'success';
    }
  }

  priorityTone(priority: Complaint['priority']): 'danger' | 'warning' | 'info' {
    switch (priority) {
      case 'high':
        return 'danger';
      case 'medium':
        return 'warning';
      case 'low':
        return 'info';
    }
  }
}
