import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { FeedbackService } from '../../../core/data/feedback.service';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';

@Component({
  selector: 'app-feedback-stats',
  standalone: true,
  imports: [
    CommonModule,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaBadgeComponent,
  ],
  templateUrl: './feedback-stats.component.html',
  styleUrl: './feedback-stats.component.css',
})
export class FeedbackStatsComponent implements OnInit {
  // Complaint statistics
  summaryReport: any = null;
  trendsReport: any = null;
  statsByCategory: any = null;
  statsByPriority: any = null;

  // Feedback statistics
  feedbackSummary: any = null;
  feedbackTrends: any = null;
  avgRatingByCategory: any = null;

  loading = false;
  error = '';

  constructor(
    private complaintService: ComplaintService,
    private feedbackService: FeedbackService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAllStats();
  }

  get isAdminOrAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_AGENT');
  }

  loadAllStats(): void {
    if (!this.isAdminOrAgent) {
      this.error = 'You do not have permission to view statistics';
      return;
    }

    this.loading = true;
    this.error = '';

    // Load complaint statistics
    this.complaintService.getSummaryReport().subscribe({
      next: (data) => {
        this.summaryReport = data;
      },
      error: () => {
        this.error = 'Error loading summary report';
      },
    });

    this.complaintService.getTrendsLastMonths(6).subscribe({
      next: (data) => {
        this.trendsReport = data;
      },
      error: () => {
        console.error('Error loading trends');
      },
    });

    this.complaintService.getStatsByCategory().subscribe({
      next: (data) => {
        this.statsByCategory = data;
      },
      error: () => {
        console.error('Error loading category stats');
      },
    });

    this.complaintService.getStatsByPriority().subscribe({
      next: (data) => {
        this.statsByPriority = data;
        this.loading = false;
      },
      error: () => {
        console.error('Error loading priority stats');
        this.loading = false;
      },
    });

    // Load feedback statistics
    this.feedbackService.getSummaryReport().subscribe({
      next: (data) => {
        this.feedbackSummary = data;
      },
      error: () => {
        console.error('Error loading feedback summary');
      },
    });

    this.feedbackService.getTrendsReport(6).subscribe({
      next: (data) => {
        this.feedbackTrends = data;
      },
      error: () => {
        console.error('Error loading feedback trends');
      },
    });

    this.feedbackService.getAvgRatingByCategory().subscribe({
      next: (data) => {
        this.avgRatingByCategory = data;
      },
      error: () => {
        console.error('Error loading rating stats');
      },
    });
  }

  // Helper methods to extract data from API responses
  getTotalComplaints(): number {
    return this.summaryReport?.totalComplaints ?? 0;
  }

  getOpenComplaints(): number {
    return this.summaryReport?.openComplaints ?? 0;
  }

  getResolvedComplaints(): number {
    return this.summaryReport?.resolvedComplaints ?? 0;
  }

  getAverageFeedbackRating(): number {
    return this.feedbackSummary?.averageRating ?? 0;
  }

  getTotalFeedback(): number {
    return this.feedbackSummary?.totalCount ?? 0;
  }

  getCategoryKeys(): string[] {
    return this.statsByCategory ? Object.keys(this.statsByCategory) : [];
  }

  getPriorityKeys(): string[] {
    return this.statsByPriority ? Object.keys(this.statsByPriority) : [];
  }

  getCategoryValue(category: string): number {
    return this.statsByCategory?.[category] ?? 0;
  }

  getPriorityValue(priority: string): number {
    return this.statsByPriority?.[priority] ?? 0;
  }
}
