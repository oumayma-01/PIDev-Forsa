import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import type { Partner, PartnerAnalytics, PartnerBadge, PartnerReview } from '../../../core/models/forsa.models';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { PartnerTypeLabelPipe } from '../partenariat-list/partenariat-type-label-public';
import { PartnerService } from '../services/partner.service';

@Component({
  selector: 'app-partner-detail',
  standalone: true,
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    RouterLink,
    ForsaCardComponent,
    ForsaButtonComponent,
    ForsaIconComponent,
    ForsaBadgeComponent,
    PartnerTypeLabelPipe,
  ],
  templateUrl: './partner-detail.component.html',
  styleUrl: './partner-detail.component.css',
})
export class PartnerDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);
  private readonly partnerService = inject(PartnerService);

  // Reactive state
  partner = signal<Partner | null>(null);
  reviews = signal<PartnerReview[]>([]);
  analytics = signal<PartnerAnalytics | null>(null);
  loading = signal(true);
  reviewsLoading = signal(false);
  analyticsLoading = signal(false);
  actionLoading = signal(false);
  reviewSubmitting = signal(false);

  // Plain properties for form inputs ([(ngModel)] requires plain props)
  showSuspendForm = false;
  suspendReason = '';
  newReviewRating = 5;
  newReviewComment = '';

  partnerId = 0;

  get isAdmin(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_ADMIN') ?? false;
  }

  get isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_CLIENT') || roles.includes('CLIENT');
  }

  ngOnInit(): void {
    this.partnerId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadPartner();
    this.loadReviews();
    if (this.isAdmin) this.loadAnalytics();
  }

  loadPartner(): void {
    this.partnerService.getPartnerById(this.partnerId).subscribe({
      next: (p) => { this.partner.set(p); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  loadReviews(): void {
    this.reviewsLoading.set(true);
    this.partnerService.getReviews(this.partnerId).subscribe({
      next: (r) => { this.reviews.set(r); this.reviewsLoading.set(false); },
      error: () => this.reviewsLoading.set(false),
    });
  }

  loadAnalytics(): void {
    this.analyticsLoading.set(true);
    this.partnerService.getPartnerAnalytics(this.partnerId).subscribe({
      next: (a) => { this.analytics.set(a); this.analyticsLoading.set(false); },
      error: () => this.analyticsLoading.set(false),
    });
  }

  activate(): void {
    const p = this.partner();
    if (!p) return;
    this.actionLoading.set(true);
    this.partnerService.activatePartner(p.id).subscribe({
      next: (updated) => { this.partner.set(updated); this.actionLoading.set(false); },
      error: () => this.actionLoading.set(false),
    });
  }

  suspend(): void {
    const p = this.partner();
    if (!p || !this.suspendReason.trim()) return;
    this.actionLoading.set(true);
    this.partnerService.suspendPartner(p.id, this.suspendReason).subscribe({
      next: (updated) => {
        this.partner.set(updated);
        this.actionLoading.set(false);
        this.showSuspendForm = false;
        this.suspendReason = '';
      },
      error: () => this.actionLoading.set(false),
    });
  }

  reactivate(): void {
    const p = this.partner();
    if (!p) return;
    this.actionLoading.set(true);
    this.partnerService.reactivatePartner(p.id).subscribe({
      next: (updated) => { this.partner.set(updated); this.actionLoading.set(false); },
      error: () => this.actionLoading.set(false),
    });
  }

  deleteReview(reviewId: number): void {
    this.partnerService.deleteReview(reviewId).subscribe({
      next: () => this.reviews.update((r) => r.filter((x) => x.id !== reviewId)),
      error: () => {},
    });
  }

  submitReview(): void {
    const p = this.partner();
    const user = this.auth.currentUser();
    if (!p || !user?.id) return;
    this.reviewSubmitting.set(true);
    this.partnerService.addReview({
      partnerId: p.id,
      clientId: Number(user.id),
      rating: this.newReviewRating,
      comment: this.newReviewComment,
    }).subscribe({
      next: (r) => {
        this.reviews.update((list) => [r, ...list]);
        this.newReviewRating = 5;
        this.newReviewComment = '';
        this.reviewSubmitting.set(false);
      },
      error: () => this.reviewSubmitting.set(false),
    });
  }

  statusTone(status: Partner['status']): 'success' | 'warning' | 'danger' | 'muted' {
    switch (status) {
      case 'ACTIVE':    return 'success';
      case 'PENDING':   return 'warning';
      case 'SUSPENDED': return 'danger';
      case 'REJECTED':  return 'danger';
      case 'CLOSED':    return 'muted';
    }
  }

  badgeTone(badge: PartnerBadge): 'default' | 'secondary' | 'warning' | 'info' | 'muted' {
    switch (badge) {
      case 'DIAMOND': return 'info';
      case 'GOLD':    return 'warning';
      case 'SILVER':  return 'secondary';
      case 'BRONZE':  return 'default';
    }
  }

  stars(n: number): number[] {
    return Array.from({ length: n }, (_, i) => i + 1);
  }

  goBack(): void {
    this.router.navigate(['/dashboard/partenariat']);
  }
}
