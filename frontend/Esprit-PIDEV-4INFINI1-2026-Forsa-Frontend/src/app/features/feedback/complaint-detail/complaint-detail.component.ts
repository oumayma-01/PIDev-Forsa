import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { ResponseService } from '../../../core/data/response.service';
import { ComplaintBackend, ComplaintResponse } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';

@Component({
  selector: 'app-complaint-detail',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ForsaBadgeComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
  ],
  templateUrl: './complaint-detail.component.html',
  styleUrl: './complaint-detail.component.css',
})
export class ComplaintDetailComponent implements OnInit {
  complaint: ComplaintBackend | null = null;
  responses: ComplaintResponse[] = [];
  newResponse = '';
  loading = false;
  loadingResponses = false;
  error = '';

  constructor(
    private complaintService: ComplaintService,
    private responseService: ResponseService,
    private router: Router,
    private route: ActivatedRoute,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadComplaint(+id);
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

  loadComplaint(id: number): void {
    this.loading = true;
    this.complaintService.getById(id).subscribe({
      next: (data) => {
        this.complaint = data;
        this.loadResponses(id);
        this.loading = false;
      },
      error: () => {
        this.error = 'Error loading complaint';
        this.loading = false;
      },
    });
  }

  loadResponses(complaintId: number): void {
    this.loadingResponses = true;
    this.responseService.getAll().subscribe({
      next: (data) => {
        // Filter responses for this complaint (assuming they're linked)
        this.responses = data; // Backend devrait filter by complaintId
        this.loadingResponses = false;
      },
      error: () => {
        this.error = 'Error loading responses';
        this.loadingResponses = false;
      },
    });
  }

  addResponse(): void {
    if (!this.newResponse.trim() || !this.complaint?.id) {
      return;
    }

    this.loading = true;
    const user = this.auth.currentUser();
    const responderName = user?.username || 'Agent';
    const responderRole = user?.roles?.[0]?.replace('ROLE_', '') || 'AGENT';

    this.complaintService
      .addResponse(
        this.complaint.id,
        this.newResponse,
        responderRole,
        responderName
      )
      .subscribe({
        next: () => {
          this.newResponse = '';
          if (this.complaint?.id) {
            this.loadResponses(this.complaint.id);
          }
          this.loading = false;
        },
        error: () => {
          this.error = 'Error adding response';
          this.loading = false;
        },
      });
  }

  deleteResponse(id: number): void {
    if (confirm('Delete this response?')) {
      this.responseService.delete(id).subscribe({
        next: () => {
          if (this.complaint?.id) {
            this.loadResponses(this.complaint.id);
          }
        },
        error: () => {
          this.error = 'Error deleting response';
        },
      });
    }
  }

  editComplaint(): void {
    if (this.complaint?.id) {
      this.router.navigate(['/dashboard/feedback/complaint', this.complaint.id, 'edit']);
    }
  }

  closeComplaint(): void {
    if (this.complaint?.id) {
      this.complaintService.close(this.complaint.id).subscribe({
        next: () => {
          this.complaint!.status = 'CLOSED';
        },
        error: () => {
          this.error = 'Error closing complaint';
        },
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard/feedback']);
  }

  goToFeedback(): void {
    this.router.navigate(['/dashboard/feedback/feedback']);
  }

  goToResponses(): void {
    this.router.navigate(['/dashboard/feedback/responses']);
  }

  goToChatbot(): void {
    this.router.navigate(['/dashboard/feedback/chatbot']);
  }

  goToStats(): void {
    this.router.navigate(['/dashboard/feedback/stats']);
  }

  statusTone(status: string): 'info' | 'warning' | 'success' | 'danger' {
    switch (status) {
      case 'OPEN':
        return 'info';
      case 'IN_PROGRESS':
        return 'warning';
      case 'RESOLVED':
        return 'success';
      case 'CLOSED':
        return 'danger';
      case 'REJECTED':
        return 'danger';
      default:
        return 'info';
    }
  }

  statusIcon(status: string): ForsaIconName {
    switch (status) {
      case 'OPEN':
        return 'alert-circle';
      case 'IN_PROGRESS':
        return 'clock';
      case 'RESOLVED':
        return 'check-circle-2';
      case 'CLOSED':
        return 'alert-circle';
      case 'REJECTED':
        return 'alert-circle';
      default:
        return 'alert-circle';
    }
  }

  priorityTone(priority: string): 'danger' | 'warning' | 'info' {
    switch (priority) {
      case 'CRITICAL':
        return 'danger';
      case 'HIGH':
        return 'danger';
      case 'MEDIUM':
        return 'warning';
      case 'LOW':
        return 'info';
      default:
        return 'info';
    }
  }
}
