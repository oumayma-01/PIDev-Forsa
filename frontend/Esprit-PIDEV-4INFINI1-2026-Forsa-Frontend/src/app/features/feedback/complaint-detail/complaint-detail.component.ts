import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintBackend, ComplaintResponse } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { FeedbackFacadeService } from '../feedback-facade.service';

type ComplaintView = ComplaintBackend & { feedback?: { id?: number } | null };

@Component({
  selector: 'app-complaint-detail',
  standalone: true,
  imports: [CommonModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent],
  templateUrl: './complaint-detail.component.html',
  styleUrl: './complaint-detail.component.css',
})
export class ComplaintDetailComponent implements OnInit {
  private readonly feedbackFacade = inject(FeedbackFacadeService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);

  complaint: ComplaintView | null = null;
  responses: ComplaintResponse[] = [];
  aiResponse = '';
  complaintId?: number;
  loading = false;
  error = '';
  financialImpact: any = null;
  financialError = '';

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.error = 'Invalid complaint id.';
      return;
    }
    this.complaintId = id;
    this.loadComplaint();
  }

  get isAdmin(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');
  }
  get isAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_AGENT') || roles.includes('AGENT');
  }
  get isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_CLIENT') || roles.includes('CLIENT');
  }
  get isAdminOrAgent(): boolean {
    return this.isAdmin || this.isAgent;
  }

  loadComplaint(): void {
    if (!this.complaintId) {
      return;
    }
    this.loading = true;
    this.error = '';
    if (this.isClient) {
      this.feedbackFacade.getMyComplaints().subscribe({
        next: (data: any) => {
          const payload = data?.data ?? data?.result ?? data?.content ?? data;
          const list = Array.isArray(payload) ? payload : [];
          const found = list.find((c: any) => Number(c?.id) === this.complaintId) ?? null;
          this.complaint = found;
          this.responses = Array.isArray(found?.responses) ? found.responses : [];
          console.log('[ComplaintDetail] client complaint from my-complaints:', found);
          if (!found) {
            this.error = 'Complaint not found for current client.';
          }
          this.loading = false;
        },
        error: (err) => {
          console.error('[ComplaintDetail] client complaint load error:', err);
          this.error = 'Unable to load complaint.';
          this.loading = false;
        },
      });
      return;
    }
    this.feedbackFacade.getComplaintById(this.complaintId).subscribe({
      next: (data: any) => {
        const payload = data?.data ?? data?.result ?? data;
        this.complaint = payload;
        const embeddedResponses = Array.isArray(payload?.responses) ? payload.responses : [];
        if (embeddedResponses.length > 0) {
          this.responses = embeddedResponses;
          this.loading = false;
        } else {
          this.loadResponses();
        }
      },
      error: () => {
        this.error = this.isClient
          ? 'Complaint detail is not available for CLIENT with current API permissions.'
          : 'Unable to load complaint.';
        this.loading = false;
      },
    });
  }

  loadResponses(): void {
    if (!this.complaintId) {
      return;
    }
    if (this.isClient) {
      this.responses = Array.isArray((this.complaint as any)?.responses) ? (this.complaint as any).responses : [];
      this.loading = false;
      return;
    }
    this.feedbackFacade.getAllResponses().subscribe({
      next: (all) => {
        const list = all;
        this.responses = (list ?? []).filter((r: any) => r.complaint?.id === this.complaintId);
        console.log('[ComplaintDetail] responses response:', all);
        this.loading = false;
      },
      error: (err) => {
        console.error('[ComplaintDetail] responses error:', err);
        this.error = 'Unable to load responses.';
        this.loading = false;
      },
    });
  }

  closeComplaint(): void {
    if (!this.complaintId) {
      return;
    }
    this.feedbackFacade.closeComplaint(this.complaintId).subscribe({
      next: () => this.loadComplaint(),
      error: () => (this.error = 'Unable to close complaint.'),
    });
  }

  generateAiResponse(): void {
    if (!this.complaintId) {
      return;
    }
    this.feedbackFacade.getAIResponse(this.complaintId).subscribe({
      next: (data: any) => {
        this.aiResponse = data?.response ?? data?.answer ?? String(data ?? '');
      },
      error: () => (this.error = 'Unable to generate AI response.'),
    });
  }

  loadFinancialImpact(): void {
    if (!this.complaintId) return;
    this.financialError = '';
    this.feedbackFacade.getFinancialImpactScore(this.complaintId).subscribe({
      next: (payload: any) => {
        this.financialImpact = payload ?? null;
      },
      error: () => {
        this.financialError = 'Unable to fetch financial impact score.';
      },
    });
  }

  impactLabel(score?: number): 'Low' | 'Medium' | 'High' {
    if (!score || score < 35) return 'Low';
    if (score < 70) return 'Medium';
    return 'High';
  }


  goBack(): void {
    this.router.navigate(['/dashboard/feedback']);
  }

  goToAddResponse(): void {
    this.router.navigate(['/dashboard/feedback/response/add'], { queryParams: { complaintId: this.complaintId } });
  }


  goToAddFeedback(): void {
    this.router.navigate(['/dashboard/feedback/feedback/add'], { queryParams: { complaintId: this.complaintId } });
  }

  canAddFeedback(): boolean {
    return this.isClient && !this.complaint?.feedback?.id;
  }

  statusTone(status?: string): 'info' | 'warning' | 'success' | 'danger' {
    if (status === 'IN_PROGRESS') return 'warning';
    if (status === 'RESOLVED') return 'success';
    if (status === 'CLOSED' || status === 'REJECTED') return 'danger';
    return 'info';
  }

  priorityTone(priority?: string): 'danger' | 'warning' | 'info' {
    if (priority === 'CRITICAL' || priority === 'HIGH') return 'danger';
    if (priority === 'MEDIUM') return 'warning';
    return 'info';
  }
}
