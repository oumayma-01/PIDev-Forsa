import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { ComplaintNotification, ComplaintNotificationService } from '../../../core/data/complaint-notification.service';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

@Component({
  selector: 'app-feedback-notifications',
  standalone: true,
  imports: [CommonModule, ForsaCardComponent, ForsaButtonComponent, ForsaIconComponent],
  templateUrl: './feedback-notifications.component.html',
})
export class FeedbackNotificationsComponent implements OnInit {
  private readonly notificationService = inject(ComplaintNotificationService);
  private readonly router = inject(Router);

  notifications: ComplaintNotification[] = [];
  loading = false;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.notificationService.getMyNotifications().subscribe({
      next: (data) => {
        this.notifications = Array.isArray(data) ? data : [];
        this.loading = false;
      },
      error: () => {
        this.notifications = [];
        this.loading = false;
      },
    });
  }

  open(n: ComplaintNotification): void {
    this.notificationService.markAsRead(n.id).subscribe({ next: () => {}, error: () => {} });
    const complaintId = n.complaint?.id;
    this.router.navigate(complaintId ? ['/dashboard/feedback/complaint', complaintId] : ['/dashboard/feedback']);
  }
}
