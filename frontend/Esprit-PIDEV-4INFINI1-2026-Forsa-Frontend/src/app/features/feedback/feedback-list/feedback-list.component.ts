import { Component, OnDestroy, OnInit, effect, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintBackend, ComplaintResponse, Feedback } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';
import { FeedbackFacadeService } from '../feedback-facade.service';
import { ComplaintNotification, ComplaintNotificationService } from '../../../core/data/complaint-notification.service';

type RoleCard = {
  icon: ForsaIconName;
  title: string;
  description: string;
  linkText: string;
  color: 'blue' | 'yellow' | 'green' | 'purple' | 'orange';
  action: 'newComplaint' | 'newFeedback' | 'openChatbot' | 'manageComplaints' | 'responses' | 'stats';
};

type ClientComplaint = ComplaintBackend & {
  feedback?: { id?: number } | null;
  user?: { id?: number } | null;
  clientId?: number;
  responses?: ComplaintResponse[];
};
type ClientFeedback = Feedback & { user?: { id?: number } | null; clientId?: number; complaint?: { id?: number } };
type AssignableAgent = { id: number; username?: string; firstName?: string; lastName?: string; email?: string };

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './feedback-list.component.html',
  styleUrl: './feedback-list.component.css',
})
export class FeedbackListComponent implements OnInit, OnDestroy {
  private readonly feedbackFacade = inject(FeedbackFacadeService);
  readonly notificationService = inject(ComplaintNotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);

items: ClientComplaint[] = [];
  filteredItems: ClientComplaint[] = [];
  complaints: ClientComplaint[] = [];
  clientComplaints: ClientComplaint[] = [];
  clientFeedbacks: ClientFeedback[] = [];
  selectedStatus = '';
  selectedCategory = '';
  filteredComplaints: ClientComplaint[] = [];
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
  showComplaintsList = false;
  roleCards: RoleCard[] = [];
  notifications: ComplaintNotification[] = [];
  unreadCount = 0;
  showNotifications = false;
  aiReportText = '';
  aiReportLoading = false;
  aiReportError = '';
  globalFeedbacks: ClientFeedback[] = [];
  globalRatingScopeLabel = 'all users';
  availableAgents: AssignableAgent[] = [];
  selectedAssignComplaintId: number | null = null;
  selectedAgentId: number | null = null;
  loadingAgents = false;
  assigningComplaint = false;

  readonly statuses = ['', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'];
  readonly categories = ['', 'TECHNICAL', 'FINANCE', 'SUPPORT', 'FRAUD', 'ACCOUNT', 'CREDIT', 'OTHER'];
  private lastRoleSignature = '__init__';

  private readonly roleSync = effect(() => {
    const roles = this.auth.currentUser()?.roles ?? [];
    this.applyRoleContext(roles);
  });

  ngOnInit(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    this.applyRoleContext(roles);
    if (this.isClient) {
      this.loadClientData();
      this.loadNotifications();
    }
    if (this.isAdminOrAgent && this.route.snapshot.routeConfig?.path === 'complaints') {
      this.showComplaintsList = true;
      this.loadComplaints();
    }
  }

  private applyRoleContext(roles: string[]): void {
    const signature = [...roles].sort().join('|');
    if (signature === this.lastRoleSignature) return;
    this.lastRoleSignature = signature;

    const hasRole = (r: string) =>
      roles.some((role) =>
        role === r ||
        role === `ROLE_${r}` ||
        role.replace('ROLE_', '') === r
      );
    this.isAdmin = hasRole('ADMIN');
    this.isAgent = hasRole('AGENT');
    this.isClient = hasRole('CLIENT') || (!this.isAdmin && !this.isAgent);
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
    } else if (this.isClient) {
      this.loadClientData();
      this.loadNotifications();
    }
  }

  ngOnDestroy(): void {}

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
    this.feedbackFacade.getAllComplaints().subscribe({
      next: (data: ComplaintBackend[]) => {
        this.items = data ?? [];
        this.filteredComplaints = [...(data ?? [])];
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Error loading complaints. Please try again.';
        this.loading = false;
      },
    });
  }

  showManageComplaints(): void {
    this.showComplaintsList = true;
    this.loadComplaints();
  }

  goBackFromComplaints(): void {
    if (this.route.snapshot.routeConfig?.path === 'complaints') {
      this.router.navigate(['/dashboard/feedback']);
      return;
    }
    this.showComplaintsList = false;
  }

  private loadClientData(): void {
    this.loadClientComplaints();
    this.loadClientFeedbacks();
    this.loadGlobalFeedbacksForRating();
  }

  loadNotifications(): void {
    this.notificationService.getMyNotifications().subscribe({
      next: (data) => {
        this.notifications = Array.isArray(data) ? data : [];
        this.unreadCount = this.notifications.filter((n) => !n.isRead).length;
        console.log('Notifications loaded:', this.notifications.length, 'Unread:', this.unreadCount);
      },
      error: (err) => {
        if (err?.status !== 401) {
          console.error('Notifications error:', err?.status, err?.message);
        }
        this.notifications = [];
        this.unreadCount = 0;
      },
    });
  }

  openAssignDialog(complaintId: number): void {
    this.selectedAssignComplaintId = complaintId;
    this.selectedAgentId = null;
    this.error = '';
    if (this.availableAgents.length > 0) return;
    this.loadingAgents = true;
    this.feedbackFacade.getAvailableAgents().subscribe({
      next: (data: any[]) => {
        this.availableAgents = (Array.isArray(data) ? data : [])
          .map((a: any) => ({
            id: Number(a?.id),
            username: a?.username,
            firstName: a?.firstName,
            lastName: a?.lastName,
            email: a?.email,
          }))
          .filter((a) => Number.isFinite(a.id) && a.id > 0);
        this.loadingAgents = false;
      },
      error: () => {
        this.loadingAgents = false;
        this.error = 'Unable to load available agents.';
      },
    });
  }

  cancelAssignDialog(): void {
    this.selectedAssignComplaintId = null;
    this.selectedAgentId = null;
  }

  confirmAssignComplaint(): void {
    if (!this.selectedAssignComplaintId) return;
    const userId = Number(this.selectedAgentId);
    if (!Number.isFinite(userId) || userId <= 0) {
      this.error = 'Please select an agent.';
      return;
    }
    this.assigningComplaint = true;
    this.feedbackFacade.assignComplaint(this.selectedAssignComplaintId, userId).subscribe({
      next: () => {
        this.error = '';
        this.assigningComplaint = false;
        this.cancelAssignDialog();
        this.loadComplaints();
      },
      error: () => {
        this.assigningComplaint = false;
        this.error = 'Unable to assign complaint.';
      },
    });
  }

  agentLabel(agent: AssignableAgent): string {
    const fullName = `${agent.firstName ?? ''} ${agent.lastName ?? ''}`.trim();
    if (fullName) return `${fullName} (#${agent.id})`;
    if (agent.username) return `${agent.username} (#${agent.id})`;
    if (agent.email) return `${agent.email} (#${agent.id})`;
    return `Agent #${agent.id}`;
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
    if (this.showNotifications && this.unreadCount > 0) {
      this.notificationService.markAllAsRead().subscribe({
        next: () => {
          this.notifications.forEach((n) => {
            n.isRead = true;
          });
          this.unreadCount = 0;
        },
      });
    }
  }

  goToComplaintFromNotif(complaintId: number, notifId: number): void {
    this.notificationService.markAsRead(notifId).subscribe();
    this.router.navigate(['/dashboard/feedback/complaint', complaintId]);
    this.showNotifications = false;
  }

  markNotificationRead(id: number): void {
    this.notificationService.markAsRead(id).subscribe({
      next: () => {
        const notif = this.notifications.find((n) => n.id === id);
        if (notif) notif.isRead = true;
        this.unreadCount = this.notifications.filter((n) => !n.isRead).length;
      },
      error: () => {
        // Keep UI state unchanged on API failure.
      },
    });
  }

  markAllNotificationsRead(): void {
    this.notificationService.markAllAsRead().subscribe({
      next: () => this.loadNotifications(),
      error: () => {
        // Keep UI state unchanged on API failure.
      },
    });
  }

  private loadClientComplaints(): void {
    this.loadingClientComplaints = true;
    this.feedbackFacade.getMyComplaints().subscribe({
      next: (data: any) => {
        const payload = data?.data ?? data?.result ?? data?.content ?? data;
        const baseList = Array.isArray(payload) ? payload : [];
        this.clientComplaints = baseList;
        this.complaints = baseList;
        this.loadingClientComplaints = false;
      },
      error: (err) => {
        if (err?.status !== 401) {
          console.error('[FeedbackList] my complaints error:', err);
        }
        this.clientComplaints = [];
        this.error = err?.status === 401 ? 'Session expired. Please login again.' : `My complaints request failed (${err?.status ?? 'no-status'}).`;
        this.loadingClientComplaints = false;
      },
    });
  }

  private loadClientFeedbacks(): void {
    this.loadingClientFeedbacks = true;
    this.feedbackFacade.getMyFeedbacks().subscribe({
      next: (data: any) => {
        let result: any[] = [];
        if (Array.isArray(data)) {
          result = data;
        } else if (Array.isArray(data?.data)) {
          result = data.data;
        } else if (Array.isArray(data?.result)) {
          result = data.result;
        } else if (Array.isArray(data?.content)) {
          result = data.content;
        } else if (data && typeof data === 'object') {
          const firstArray = Object.values(data).find(v => Array.isArray(v));
          result = (firstArray as any[]) ?? [];
        }
        this.clientFeedbacks = result;
        this.loadingClientFeedbacks = false;
      },
      error: (err) => {
        if (err?.status !== 401) {
          console.error('[DEBUG] feedbacks error status:', err?.status);
        }
        this.clientFeedbacks = [];
        this.loadingClientFeedbacks = false;
      },
    });
  }

  private loadGlobalFeedbacksForRating(): void {
    if (this.isClient) {
      this.globalFeedbacks = this.clientFeedbacks;
      this.globalRatingScopeLabel = 'your feedbacks';
      return;
    }
    this.feedbackFacade.getAllFeedbacks().subscribe({
      next: (data: any) => {
        const list = Array.isArray(data) ? data : [];
        this.globalFeedbacks = list;
        this.globalRatingScopeLabel = 'all users';
      },
      error: () => {
        this.globalFeedbacks = this.clientFeedbacks;
        this.globalRatingScopeLabel = 'your feedbacks';
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
    this.feedbackFacade.deleteComplaint(id).subscribe({
      next: () => this.loadComplaints(),
      error: () => (this.error = 'Unable to delete complaint.'),
    });
  }

  deleteComplaint(id: number): void {
    if (!confirm('Are you sure you want to cancel this complaint?')) return;
    this.feedbackFacade.deleteComplaint(id).subscribe({
      next: () => {
        this.clientComplaints = this.clientComplaints.filter(c => c.id !== id);
        this.complaints = this.complaints.filter(c => c.id !== id);
      },
      error: () => {
        this.error = 'Unable to cancel complaint.';
      },
    });
  }

  close(id: number): void {
    this.feedbackFacade.closeComplaint(id).subscribe({
      next: () => this.loadComplaints(),
      error: () => (this.error = 'Unable to close complaint.'),
    });
  }

  deleteFeedback(id: number): void {
    if (!confirm('Delete this feedback?')) return;
    this.feedbackFacade.deleteFeedback(id).subscribe({
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
      this.router.navigate(['/dashboard/feedback/complaints']);
    }
  }

  feedbackStars(rating: number): string {
    const rounded = Math.max(0, Math.min(5, Math.round(rating || 0)));
    return '★★★★★'.slice(0, rounded) + '☆☆☆☆☆'.slice(0, 5 - rounded);
  }

  get clientAverageRating(): number {
    if (!this.globalFeedbacks.length) return 0;
    const sum = this.globalFeedbacks.reduce((acc, item) => acc + Number(item.rating ?? 0), 0);
    return Math.round((sum / this.globalFeedbacks.length) * 100) / 100;
  }

  get clientAverageRatingStars(): string {
    return this.feedbackStars(this.clientAverageRating);
  }


  loadAiReport(): void {
    this.aiReportLoading = true;
    this.aiReportError = '';
    this.aiReportText = '';
    this.feedbackFacade.getAIFullReport().subscribe({
      next: (payload: any) => {
        this.aiReportText = String(payload?.insights ?? '').trim();
        if (!this.aiReportText) {
          this.aiReportError = 'AI report is currently empty.';
        }
        this.aiReportLoading = false;
      },
      error: () => {
        this.aiReportLoading = false;
        this.aiReportError = 'Unable to generate AI report right now.';
      },
    });
  }
}
