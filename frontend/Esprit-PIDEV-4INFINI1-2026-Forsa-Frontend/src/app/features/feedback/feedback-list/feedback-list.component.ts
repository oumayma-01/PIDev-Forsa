import { Component, OnDestroy, OnInit, effect, inject } from '@angular/core';
import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { FeedbackService } from '../../../core/data/feedback.service';
import { ComplaintBackend, ComplaintResponse, Feedback } from '../../../core/models/forsa.models';
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

type ClientComplaint = ComplaintBackend & {
  feedback?: { id?: number } | null;
  user?: { id?: number } | null;
  clientId?: number;
  responses?: ComplaintResponse[];
};
type ClientFeedback = Feedback & { user?: { id?: number } | null; clientId?: number; complaint?: { id?: number } };

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, DecimalPipe, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './feedback-list.component.html',
  styleUrl: './feedback-list.component.css',
})
export class FeedbackListComponent implements OnInit, OnDestroy {
  private readonly complaintService = inject(ComplaintService);
  private readonly feedbackService = inject(FeedbackService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  items: ClientComplaint[] = [];
  filteredItems: ClientComplaint[] = [];
  complaints: ClientComplaint[] = [];
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
  private lastRoleSignature = '__init__';
  private clientPollId: ReturnType<typeof setInterval> | null = null;

  private readonly roleSync = effect(() => {
    const roles = this.auth.currentUser()?.roles ?? [];
    this.applyRoleContext(roles);
  });

  ngOnInit(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    console.log('[DEBUG] roles:', this.auth.currentUser()?.roles);
    this.applyRoleContext(roles);
    console.log('[DEBUG] isClient:', this.isClient);
    // Always load client data on init as fallback
    this.loadClientData();
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
      this.startClientPolling();
    }
  }

  ngOnDestroy(): void {
    if (this.clientPollId) {
      clearInterval(this.clientPollId);
      this.clientPollId = null;
    }
  }

  private startClientPolling(): void {
    if (this.clientPollId) return;
    this.clientPollId = setInterval(() => {
      if (this.isClient) {
        this.loadClientComplaints();
      }
    }, 10000);
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
    console.log('[DEBUG] loadClientData called');
    this.loadClientComplaints();
    this.loadClientFeedbacks();
  }

  private loadClientComplaints(): void {
    this.loadingClientComplaints = true;
    this.complaintService.getMyComplaints().subscribe({
      next: (data: any) => {
        console.log('[DEBUG] raw complaints response:', data);
        const payload = data?.data ?? data?.result ?? data?.content ?? data;
        const baseList = Array.isArray(payload) ? payload : [];
        console.log('[DEBUG] parsed complaints:', baseList);
        if (!baseList.length) {
          this.clientComplaints = [];
          this.complaints = [];
          this.loadingClientComplaints = false;
          // do not return — allow UI to update
        } else {
          forkJoin(
            baseList.map((c: any) =>
              this.complaintService.getById(c.id).pipe(
                map((full: any) => {
                  const fullPayload = full?.data ?? full?.result ?? full;
                  return { ...c, responses: fullPayload?.responses ?? c?.responses ?? [] } as ClientComplaint;
                }),
                catchError(() => of({ ...c, responses: c?.responses ?? [] } as ClientComplaint)),
              ),
            ),
          ).subscribe({
            next: (fullList) => {
              this.clientComplaints = fullList;
              this.complaints = fullList;
              this.loadingClientComplaints = false;
            },
            error: () => {
              this.clientComplaints = baseList;
              this.complaints = baseList;
              this.loadingClientComplaints = false;
            },
          });
        }
      },
      error: (err) => {
        console.error('[FeedbackList] my complaints error:', err);
        this.clientComplaints = [];
        this.error = `My complaints request failed (${err?.status ?? 'no-status'}).`;
        this.loadingClientComplaints = false;
      },
    });
  }

  private loadClientFeedbacks(): void {
    this.loadingClientFeedbacks = true;
    this.feedbackService.getMyFeedbacks().subscribe({
      next: (data: any) => {
        console.log('[DEBUG] raw feedbacks response:', data);
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
        console.log('[DEBUG] parsed feedbacks count:', result.length);
        this.clientFeedbacks = result;
        this.loadingClientFeedbacks = false;
      },
      error: (err) => {
        console.error('[DEBUG] feedbacks error status:', err?.status);
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

  deleteComplaint(id: number): void {
    if (!confirm('Are you sure you want to cancel this complaint?')) return;
    this.complaintService.delete(id).subscribe({
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
