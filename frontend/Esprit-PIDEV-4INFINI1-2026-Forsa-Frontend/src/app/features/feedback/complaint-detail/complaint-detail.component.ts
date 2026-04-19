import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { ResponseService } from '../../../core/data/response.service';
import { ComplaintBackend, ComplaintResponse } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';

type ComplaintView = ComplaintBackend & { feedback?: { id?: number } | null };

@Component({
  selector: 'app-complaint-detail',
  standalone: true,
  imports: [CommonModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent],
  templateUrl: './complaint-detail.component.html',
  styleUrl: './complaint-detail.component.css',
})
export class ComplaintDetailComponent implements OnInit {
  private readonly complaintService = inject(ComplaintService);
  private readonly responseService = inject(ResponseService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);

  complaint: ComplaintView | null = null;
  responses: ComplaintResponse[] = [];
  aiResponse = '';
  complaintId?: number;
  loading = false;
  error = '';

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
    return this.auth.currentUser()?.roles?.includes('ROLE_ADMIN') ?? false;
  }
  get isAgent(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_AGENT') ?? false;
  }
  get isClient(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_CLIENT') ?? false;
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
    this.complaintService.getById(this.complaintId).subscribe({
      next: (data: any) => {
        this.complaint = data;
        this.loadResponses();
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
    this.responseService.getAll().subscribe({
      next: (all) => {
        this.responses = (all ?? []).filter((r) => r.complaint?.id === this.complaintId);
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load responses.';
        this.loading = false;
      },
    });
  }

  closeComplaint(): void {
    if (!this.complaintId) {
      return;
    }
    this.complaintService.close(this.complaintId).subscribe({
      next: () => this.loadComplaint(),
      error: () => (this.error = 'Unable to close complaint.'),
    });
  }

  generateAiResponse(): void {
    if (!this.complaintId) {
      return;
    }
    this.complaintService.getAIResponse(this.complaintId).subscribe({
      next: (data: any) => {
        this.aiResponse = data?.response ?? data?.answer ?? String(data ?? '');
      },
      error: () => (this.error = 'Unable to generate AI response.'),
    });
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
