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

type ClientComplaint = ComplaintBackend & { feedback?: unknown };

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
  readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  items: ComplaintBackend[] = [];
  filteredItems: ComplaintBackend[] = [];
  clientComplaints: ComplaintBackend[] = [];
  clientFeedbacks: Feedback[] = [];
  clientComplaintsLoading = false;
  clientFeedbacksLoading = false;
  clientComplaintsError = '';
  clientFeedbacksError = '';
  loading = false;
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
    console.log('=== FEEDBACK LIST INIT ===');
    console.log('currentUser:', this.auth.currentUser());
    console.log('roles:', this.auth.currentUser()?.roles);

    const user = this.auth.currentUser();
    if (!user) {
      const interval = setInterval(() => {
        if (this.auth.currentUser()) {
          clearInterval(interval);
          this.initByRole();
        }
      }, 100);
      return;
    }
    this.initByRole();
  }

  private initByRole(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    this.isClient = roles.some((r) => r === 'ROLE_CLIENT' || r === 'CLIENT' || r.includes('CLIENT'));
    this.isAdmin = roles.some((r) => r === 'ROLE_ADMIN' || r === 'ADMIN' || r.includes('ADMIN'));
    this.isAgent = roles.some((r) => r === 'ROLE_AGENT' || r === 'AGENT' || r.includes('AGENT'));
    this.isAdminOrAgent = this.isAdmin || this.isAgent;

    console.log('isClient:', this.isClient);
    console.log('isAdmin:', this.isAdmin);
    console.log('isAgent:', this.isAgent);

    this.pageDescription = this.isAdmin
      ? 'Full oversight of feedback and complaints'
      : this.isAgent
        ? 'Manage complaints and responses'
        : 'Submit feedback or track your complaints';
    this.roleCards = this.buildRoleCards();
    this.showComplaintsSection = this.isAdminOrAgent;

    console.log('INIT BY ROLE - isClient:', this.isClient);
    if (this.isClient) {
      this.loadClientComplaints();
      this.loadClientFeedbacks();
    } else if (this.isAdmin || this.isAgent) {
      this.loadComplaints();
    } else {
      this.loadClientComplaints();
      this.loadClientFeedbacks();
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
      next: (data: ComplaintBackend[]) => {
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

  loadClientComplaints(): void {
    console.log('=== LOADING CLIENT COMPLAINTS ===');
    this.clientComplaintsLoading = true;
    this.clientComplaintsError = '';
    this.complaintService.getMyComplaints().subscribe({
      next: (data: ComplaintBackend[]) => {
        console.log('CLIENT COMPLAINTS RESPONSE:', data);
        console.log('COUNT:', data.length);
        this.clientComplaints = data;
        this.clientComplaintsLoading = false;
      },
      error: (err) => {
        console.error('CLIENT COMPLAINTS ERROR:', err);
        console.error('STATUS:', err?.status);
        console.error('MESSAGE:', err?.error);
        this.clientComplaintsError = 'Could not load your complaints.';
        this.clientComplaintsLoading = false;
      }
    });
  }

  loadClientFeedbacks(): void {
    console.log('=== LOADING CLIENT FEEDBACKS ===');
    this.clientFeedbacksLoading = true;
    this.clientFeedbacksError = '';
    this.feedbackService.getMyFeedbacks().subscribe({
      next: (data: Feedback[]) => {
        console.log('CLIENT FEEDBACKS RESPONSE:', data);
        console.log('COUNT:', data.length);
        this.clientFeedbacks = data.slice(0, 5);
        this.clientFeedbacksLoading = false;
      },
      error: (err) => {
        console.error('CLIENT FEEDBACKS ERROR:', err);
        console.error('STATUS:', err?.status);
        this.clientFeedbacksError = 'Could not load your feedbacks.';
        this.clientFeedbacksLoading = false;
      }
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

  goToComplaintDetail(id: number): void {
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
    this.deleteMyFeedback(id);
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

  canAddFeedback(item: ClientComplaint): boolean {
    return !item.feedback;
  }

  goToAddFeedback(complaintId: number): void {
    this.router.navigate(['/dashboard/feedback/feedback/add'], { queryParams: { complaintId } });
  }

  getStars(rating: number): string {
    const r = Math.max(0, Math.min(5, Math.round(rating ?? 0)));
    return '★'.repeat(r) + '☆'.repeat(5 - r);
  }

  getStatusTone(status: string): 'info' | 'warning' | 'success' | 'danger' {
    switch (status) {
      case 'OPEN': return 'info';
      case 'IN_PROGRESS': return 'warning';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'danger';
      case 'REJECTED': return 'danger';
      default: return 'info';
    }
  }

  getPriorityTone(priority: string): 'info' | 'warning' | 'danger' {
    switch (priority) {
      case 'CRITICAL': return 'danger';
      case 'HIGH': return 'danger';
      case 'MEDIUM': return 'warning';
      case 'LOW': return 'info';
      default: return 'info';
    }
  }

  deleteMyFeedback(id: number): void {
    if (confirm('Delete this feedback?')) {
      this.feedbackService.delete(id).subscribe({
        next: () => this.loadClientFeedbacks(),
        error: () => this.clientFeedbacksError = 'Delete failed.'
      });
    }
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
}
