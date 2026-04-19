import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { FeedbackService } from '../../../core/data/feedback.service';
import { ComplaintBackend, Feedback } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';

type RoleCard = {
  icon: ForsaIconName;
  title: string;
  description: string;
  linkText: string;
  color: 'blue' | 'yellow' | 'green' | 'purple' | 'orange';
  action: 'newComplaint' | 'newFeedback' | 'openChatbot' | 'manageComplaints' | 'responses' | 'stats';
};

type ClientComplaint = ComplaintBackend & { feedback?: { id?: number } | null; user?: { id?: number } | null; clientId?: number };
type ClientFeedback = Feedback & { user?: { id?: number } | null; clientId?: number; complaint?: { id?: number } };

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './feedback-list.component.html',
  styleUrl: './feedback-list.component.css',
})
export class FeedbackListComponent implements OnInit {
  private readonly complaintService = inject(ComplaintService);
  private readonly feedbackService = inject(FeedbackService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  items: ClientComplaint[] = [];
  filteredItems: ClientComplaint[] = [];
  clientComplaints: ClientComplaint[] = [];
  clientFeedbacks: ClientFeedback[] = [];
  loading = false;
  loadingClientComplaints = false;
  loadingClientFeedbacks = false;
  error = '';
  statusFilter = '';
  categoryFilter = '';
  pageDescription = 'Manage complaints and responses.';
  isAdmin = false;
  isAgent = false;
  isClient = false;
  isAdminOrAgent = false;
  showComplaintsSection = false;
  roleCards: RoleCard[] = [];

  readonly statuses = ['', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'];
  readonly categories = ['', 'TECHNICAL', 'FINANCE', 'SUPPORT', 'FRAUD', 'ACCOUNT', 'CREDIT', 'OTHER'];

  ngOnInit(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    this.isAdmin = roles.includes('ROLE_ADMIN');
    this.isAgent = roles.includes('ROLE_AGENT');
    this.isClient = roles.includes('ROLE_CLIENT');
    this.isAdminOrAgent = this.isAdmin || this.isAgent;
    this.pageDescription = this.isAdmin
      ? 'Full oversight of feedback and complaints'
      : this.isAgent
        ? 'Manage complaints and responses'
        : 'Submit feedback or track your complaints';
    this.roleCards = this.buildRoleCards();
    this.showComplaintsSection = this.isAdminOrAgent;

    if (this.isAdminOrAgent) {
      this.loadComplaints();
    } else {
      this.loadClientData();
    }
  }

  private buildRoleCards(): RoleCard[] {
    if (this.isClient) {
      return [
        { icon: 'message-square', title: 'My Complaints', description: 'Submit and track your complaints', linkText: 'New Complaint ->', color: 'blue', action: 'newComplaint' },
        { icon: 'heart', title: 'My Feedbacks', description: 'Rate your experience and share feedback', linkText: 'Add Feedback ->', color: 'yellow', action: 'newFeedback' },
        { icon: 'message-square', title: 'Virtual Assistant', description: 'Chat with our AI assistant for help', linkText: 'Open Chatbot ->', color: 'green', action: 'openChatbot' },
      ];
    }
    if (this.isAgent) {
      return [
        { icon: 'layout-dashboard', title: 'Complaints Management', description: 'View and manage all client complaints', linkText: 'Manage Complaints ->', color: 'blue', action: 'manageComplaints' },
        { icon: 'send', title: 'Responses', description: 'Manage responses to complaints', linkText: 'Manage Responses ->', color: 'green', action: 'responses' },
        { icon: 'message-square', title: 'Virtual Assistant', description: 'Use AI chatbot for assistance', linkText: 'Open Chatbot ->', color: 'purple', action: 'openChatbot' },
      ];
    }
    return [
      { icon: 'layout-dashboard', title: 'Complaints Management', description: 'Full complaints management and oversight', linkText: 'Manage Complaints ->', color: 'blue', action: 'manageComplaints' },
      { icon: 'bar-chart-3', title: 'Statistics & Reports', description: 'View analytics, trends and performance metrics', linkText: 'View Stats ->', color: 'orange', action: 'stats' },
      { icon: 'send', title: 'Responses Management', description: 'Manage and improve responses with AI', linkText: 'Manage Responses ->', color: 'green', action: 'responses' },
      { icon: 'message-square', title: 'Virtual Assistant', description: 'AI-powered assistant for complaint handling', linkText: 'Open Chatbot ->', color: 'purple', action: 'openChatbot' },
    ];
  }

  loadComplaints(): void {
    this.loading = true;
    this.error = '';
    this.complaintService.getAll().subscribe({
      next: (data: any[]) => {
        this.items = data ?? [];
        this.applyFilters();
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load complaints. Please retry.';
        this.loading = false;
      },
    });
  }

  private loadClientData(): void {
    this.loadClientComplaints();
    this.loadClientFeedbacks();
  }

  private loadClientComplaints(): void {
    this.loadingClientComplaints = true;
    this.complaintService.getMyComplaints().subscribe({
      next: (data: any[]) => {
        this.clientComplaints = data ?? [];
        this.loadingClientComplaints = false;
      },
      error: () => {
        this.clientComplaints = [];
        this.loadingClientComplaints = false;
      },
    });
  }

  private loadClientFeedbacks(): void {
    this.loadingClientFeedbacks = true;
    this.feedbackService.getMyFeedbacks().subscribe({
      next: (data: any[]) => {
        this.clientFeedbacks = data ?? [];
        this.loadingClientFeedbacks = false;
      },
      error: () => {
        this.clientFeedbacks = [];
        this.loadingClientFeedbacks = false;
      },
    });
  }

  applyFilters(): void {
    this.filteredItems = this.items.filter((item) => {
      const statusOk = this.statusFilter ? item.status === this.statusFilter : true;
      const categoryOk = this.categoryFilter ? item.category === this.categoryFilter : true;
      return statusOk && categoryOk;
    });
  }

  goToAdd(): void {
    this.router.navigate(['/dashboard/feedback/complaint/add']);
  }

  goToDetail(id: number): void {
    this.router.navigate(['/dashboard/feedback/complaint', id]);
  }

  goToEditComplaint(id: number): void {
    this.router.navigate(['/dashboard/feedback/complaint', id, 'edit']);
  }

  goToEditFeedback(id: number): void {
    this.router.navigate(['/dashboard/feedback/feedback', id]);
  }

  goToAddFeedbackDirect(): void {
    this.router.navigate(['/dashboard/feedback/feedback/add']);
  }

  goToChatbot(): void {
    this.router.navigate(['/dashboard/feedback/chatbot']);
  }

  goToResponses(): void {
    this.router.navigate(['/dashboard/feedback/responses']);
  }

  goToStats(): void {
    this.router.navigate(['/dashboard/feedback/stats']);
  }

  goToAddResponse(complaintId: number): void {
    this.router.navigate(['/dashboard/feedback/response/add'], { queryParams: { complaintId } });
  }

  delete(id: number): void {
    if (!confirm('Delete this complaint?')) return;
    this.complaintService.delete(id).subscribe({
      next: () => this.loadComplaints(),
      error: () => (this.error = 'Unable to delete complaint.'),
    });
  }

  close(id: number): void {
    this.complaintService.close(id).subscribe({
      next: () => this.loadComplaints(),
      error: () => (this.error = 'Unable to close complaint.'),
    });
  }

  deleteFeedback(id: number): void {
    if (!confirm('Delete this feedback?')) return;
    this.feedbackService.delete(id).subscribe({
      next: () => {
        this.clientFeedbacks = this.clientFeedbacks.filter((f) => f.id !== id);
      },
      error: () => {
        // Keep local view unchanged when delete fails.
      },
    });
  }

  statusIcon(status: string): ForsaIconName {
    if (status === 'IN_PROGRESS') return 'clock';
    if (status === 'RESOLVED') return 'check-circle-2';
    return 'alert-circle';
  }

  statusTone(status: string): 'info' | 'warning' | 'success' | 'danger' {
    if (status === 'IN_PROGRESS') return 'warning';
    if (status === 'RESOLVED') return 'success';
    if (status === 'CLOSED' || status === 'REJECTED') return 'danger';
    return 'info';
  }

  priorityTone(priority: string): 'danger' | 'warning' | 'info' {
    if (priority === 'CRITICAL' || priority === 'HIGH') return 'danger';
    if (priority === 'MEDIUM') return 'warning';
    return 'info';
  }

  canAddFeedback(item: { feedback?: { id?: number } | null }): boolean {
    return !item.feedback?.id;
  }

  goToAddFeedback(complaintId: number): void {
    this.router.navigate(['/dashboard/feedback/feedback/add'], { queryParams: { complaintId } });
  }

  onRoleCardClick(action: RoleCard['action']): void {
    if (action === 'newComplaint') this.goToAdd();
    if (action === 'newFeedback') this.router.navigate(['/dashboard/feedback/feedback/add']);
    if (action === 'openChatbot') this.goToChatbot();
    if (action === 'responses') this.goToResponses();
    if (action === 'stats') this.goToStats();
    if (action === 'manageComplaints') {
      this.showComplaintsSection = true;
      if (!this.loading && this.items.length === 0) this.loadComplaints();
    }
  }

  feedbackStars(rating: number): string {
    const rounded = Math.max(0, Math.min(5, Math.round(rating || 0)));
    return '★★★★★'.slice(0, rounded) + '☆☆☆☆☆'.slice(0, 5 - rounded);
  }
}
