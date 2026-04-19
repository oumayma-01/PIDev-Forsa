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
  avgRatingByCategory: Record<string, number> = {};
  private complaintsRaw: any[] = [];

  // Make Object available to template
  Object = Object;

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
    console.log('🔄 Loading statistics from backend...');

    // Load complaint statistics
    this.complaintService.getSummaryReport().subscribe({
      next: (data) => {
        console.log('📊 Summary Report received:', data);
        this.summaryReport = data;
      },
      error: (err) => {
        console.error('❌ Error loading summary report:', err);
        this.error = 'Error loading complaint summary';
      },
    });

    this.complaintService.getAll().subscribe({
      next: (data) => {
        this.complaintsRaw = Array.isArray(data) ? data : [];
        if (Object.keys(this.avgRatingByCategory).length === 0) {
          this.applyFallbackCategoryRatings();
        }
      },
      error: (err) => {
        console.error('Error loading complaints for rating fallback:', err);
      },
    });

    this.complaintService.getStatsByCategory().subscribe({
      next: (data) => {
        console.log('📂 Category Stats received:', data);
        this.statsByCategory = data;
        this.seedRatingsFromComplaintCategories();
      },
      error: (err) => {
        console.error('❌ Error loading category stats:', err);
      },
    });

    this.complaintService.getStatsByPriority().subscribe({
      next: (data) => {
        console.log('⭐ Priority Stats received:', data);
        this.statsByPriority = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Error loading priority stats:', err);
        this.loading = false;
      },
    });

    this.feedbackService.getSummaryReport().subscribe({
      next: (data) => {
        console.log('📝 Feedback Summary received:', data);
        this.feedbackSummary = data;
      },
      error: (err) => {
        console.error('❌ Error loading feedback summary:', err);
      },
    });

    this.feedbackService.getAvgRatingByCategory().subscribe({
      next: (data) => {
        console.log('⭐ Rating by Category received:', data);
        console.log('⭐ Type:', typeof data);
        console.log('⭐ Is Array:', Array.isArray(data));

        const normalized = this.normalizeAvgRatingByCategory(data);
        this.avgRatingByCategory = {
          ...this.avgRatingByCategory,
          ...normalized,
        };
        console.log('⭐ Normalized rating by category:', this.avgRatingByCategory);

        if (Object.keys(this.avgRatingByCategory).length === 0) {
          this.applyFallbackCategoryRatings();
        }
      },
      error: (err) => {
        console.error('❌ Error loading rating stats:', err);
        this.seedRatingsFromComplaintCategories();
        this.applyFallbackCategoryRatings();
      },
    });
  }

  private seedRatingsFromComplaintCategories(): void {
    if (!this.statsByCategory || typeof this.statsByCategory !== 'object') {
      return;
    }

    const seeded: Record<string, number> = { ...this.avgRatingByCategory };
    Object.keys(this.statsByCategory).forEach((category) => {
      if (!Number.isFinite(seeded[category])) {
        seeded[category] = 0;
      }
    });
    this.avgRatingByCategory = seeded;
  }

  private normalizeAvgRatingByCategory(data: any): Record<string, number> {
    const normalized: Record<string, number> = {};

    if (!data) {
      return normalized;
    }

    if (Array.isArray(data)) {
      data.forEach((item: any) => {
        if (!item || typeof item !== 'object') {
          return;
        }

        const category = item.category ?? item.CATEGORY ?? item.complaintCategory;
        const ratingValue = item.avgRating ?? item.averageRating ?? item.AVG_RATING ?? item.value;
        const numericRating = Number(ratingValue);

        if (category && Number.isFinite(numericRating)) {
          normalized[String(category)] = numericRating;
          return;
        }

        const keys = Object.keys(item);
        if (keys.length === 1) {
          const singleKey = keys[0];
          const singleValue = Number(item[singleKey]);
          if (singleKey && Number.isFinite(singleValue)) {
            normalized[String(singleKey)] = singleValue;
          }
        }
      });
      return normalized;
    }

    if (typeof data === 'object') {
      Object.entries(data).forEach(([key, value]) => {
        const numericValue = Number(value);
        if (Number.isFinite(numericValue)) {
          normalized[key] = numericValue;
        }
      });
    }

    return normalized;
  }

  private applyFallbackCategoryRatings(): void {
    if (!Array.isArray(this.complaintsRaw) || this.complaintsRaw.length === 0) {
      return;
    }

    const byCategory: Record<string, { sum: number; count: number }> = {};

    this.complaintsRaw.forEach((complaint: any) => {
      const category = complaint?.category;
      const rating = complaint?.feedback?.rating;
      const numericRating = Number(rating);

      if (!category || !Number.isFinite(numericRating)) {
        return;
      }

      if (!byCategory[category]) {
        byCategory[category] = { sum: 0, count: 0 };
      }

      byCategory[category].sum += numericRating;
      byCategory[category].count += 1;
    });

    const fallback: Record<string, number> = {};
    Object.entries(byCategory).forEach(([category, stats]) => {
      if (stats.count > 0) {
        fallback[category] = stats.sum / stats.count;
      }
    });

    if (Object.keys(fallback).length > 0) {
      this.avgRatingByCategory = fallback;
      console.log('⭐ Applied fallback rating by category:', fallback);
    }
  }

  // Helper methods to extract data from API responses
  getTotalComplaints(): number {
    const value = this.summaryReport?.total
      ?? this.summaryReport?.totalComplaints
      ?? 0;
    return value;
  }

  getOpenComplaints(): number {
    const value = this.summaryReport?.byStatus?.OPEN
      ?? this.summaryReport?.openComplaints
      ?? 0;
    return value;
  }

  getResolvedComplaints(): number {
    const value = this.summaryReport?.byStatus?.RESOLVED
      ?? this.summaryReport?.resolvedComplaints
      ?? 0;
    return value;
  }

  getAverageFeedbackRating(): number {
    const value = this.feedbackSummary?.avgRating
      ?? this.feedbackSummary?.averageRating
      ?? 0;
    return value;
  }

  getTotalFeedback(): number {
    const value = this.feedbackSummary?.total
      ?? this.feedbackSummary?.totalCount
      ?? 0;
    return value;
  }

  getCategoryKeys(): string[] {
    // Si on a avgRatingByCategory avec données, utiliser ses clés
    const keys = new Set<string>();
    if (this.avgRatingByCategory && Object.keys(this.avgRatingByCategory).length > 0) {
      Object.keys(this.avgRatingByCategory).forEach((k) => keys.add(k));
    }
    // Sinon utiliser les catégories de complaints (fallback)
    if (this.statsByCategory && Object.keys(this.statsByCategory).length > 0) {
      Object.keys(this.statsByCategory).forEach((k) => keys.add(k));
    }
    return Array.from(keys);
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

  getCategoryRating(category: string): number {
    const rating = this.avgRatingByCategory?.[category];
    if (rating !== undefined && rating !== null && Number.isFinite(Number(rating))) {
      return rating;
    }
    return 0;
  }
}
