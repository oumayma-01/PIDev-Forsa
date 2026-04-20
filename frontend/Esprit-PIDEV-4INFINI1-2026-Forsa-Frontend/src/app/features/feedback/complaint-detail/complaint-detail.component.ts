import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { ResponseService } from '../../../core/data/response.service';
import { ComplaintBackend } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';

type ComplaintView = ComplaintBackend & { feedback?: { id?: number } | null };

@Component({
  selector: 'app-complaint-detail',
  standalone: true,
  imports: [CommonModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
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
  responses: any[] = [];
  aiResponse: string | null = null;
  complaintId = 0;
  loading = false;
  responsesLoading = false;
  error = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.complaintId = +id;
      this.loadComplaint();
      this.loadResponses();
    }
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
    this.loading = true;
    this.complaintService.getById(this.complaintId).subscribe({
      next: (data) => {
        this.complaint = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  loadResponses(): void {
    this.responsesLoading = true;
    this.responseService.getAll().subscribe({
      next: (data: any[]) => {
        this.responses = data.filter((r) => r.complaint?.id === this.complaintId);
        this.responsesLoading = false;
      },
      error: () => {
        this.responsesLoading = false;
      }
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

  generateAIResponse(): void {
    this.complaintService.getAIResponse(this.complaintId).subscribe({
      next: (res: any) => {
        this.aiResponse = res.response ?? res.answer ?? res.message ?? JSON.stringify(res);
      },
      error: () => {
        this.aiResponse = 'Could not generate AI response.';
      }
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
