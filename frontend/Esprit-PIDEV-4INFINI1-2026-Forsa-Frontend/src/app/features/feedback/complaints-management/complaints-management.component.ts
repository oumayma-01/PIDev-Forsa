import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FeedbackFacadeService } from '../feedback-facade.service';
import { ComplaintBackend } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-complaints-management',
  standalone: true,
  imports: [CommonModule, FormsModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent, ForsaInputDirective],
  templateUrl: './complaints-management.component.html',
  styleUrl: './complaints-management.component.css',
})
export class ComplaintsManagementComponent implements OnInit {
  private readonly facade = inject(FeedbackFacadeService);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  items: (ComplaintBackend & { user?: { id?: number; fullName?: string; name?: string; username?: string; login?: string; email?: string } | null; clientId?: number; })[] = [];
  filteredItems: (ComplaintBackend & { user?: { id?: number; fullName?: string; name?: string; username?: string; login?: string; email?: string } | null; clientId?: number; })[] = [];
  loading = false;
  error = '';
  aiResponseByComplaint: Record<number, string> = {};
  impactScoreByComplaint: Record<number, number> = {};
  statusFilter = '';
  categoryFilter = '';
  readonly statuses = ['', 'OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'];
  readonly categories = ['', 'TECHNICAL', 'FINANCE', 'SUPPORT', 'FRAUD', 'ACCOUNT', 'CREDIT', 'OTHER'];
  isAdmin = false;

  ngOnInit(): void {
    const roles = this.auth.currentUser()?.roles ?? [];
    this.isAdmin = roles.some((role) => role === 'ADMIN' || role === 'ROLE_ADMIN' || role.replace('ROLE_', '') === 'ADMIN');
    this.load();
  }
  load(): void {
    this.loading = true;
    this.facade.getAllComplaints().subscribe({
      next: (d) => {
        this.items = d ?? [];
        this.applyFilters();
        this.items.forEach((c) => { if (c.id) this.loadImpactScore(c.id); });
        this.loading = false;
      },
      error: () => { this.error = 'Error loading complaints'; this.loading = false; },
    });
  }
  applyFilters(): void {
    this.filteredItems = this.items.filter((x) =>
      (!this.statusFilter || x.status === this.statusFilter) &&
      (!this.categoryFilter || x.category === this.categoryFilter));
  }
  goBack(): void { this.router.navigate(['/dashboard/feedback']); }
  viewDetails(id: number): void { this.router.navigate(['/dashboard/feedback/complaint', id]); }
  addResponse(id: number): void { this.router.navigate(['/dashboard/feedback/response/add'], { queryParams: { complaintId: id } }); }
  closeComplaint(id: number): void {
    this.facade.closeComplaint(id).subscribe({ next: () => this.load(), error: () => { this.error = 'Unable to close complaint'; } });
  }
  deleteComplaint(id: number): void {
    if (!confirm('Delete this complaint?')) return;
    this.facade.deleteComplaint(id).subscribe({ next: () => this.load(), error: () => { this.error = 'Unable to delete complaint'; } });
  }
  generateAiResponse(id: number): void {
    this.facade.getAIResponse(id).subscribe({
      next: (res) => { this.aiResponseByComplaint[id] = String(res?.response ?? '').trim(); },
      error: () => { this.aiResponseByComplaint[id] = 'Unable to generate AI response.'; },
    });
  }
  loadImpactScore(id: number): void {
    this.facade.getFinancialImpactScore(id).subscribe({
      next: (res) => { this.impactScoreByComplaint[id] = Number(res?.financialImpactScore ?? 0); },
      error: () => {},
    });
  }

  complaintAuthor(item: any): string {
    const user = item?.user ?? item?.client ?? item?.author ?? item?.createdBy;
    const fullName = user?.fullName ?? user?.name;
    const username = user?.username ?? user?.login;
    const email = user?.email;
    const id = user?.id ?? item?.clientId;
    return fullName || username || email || (id ? `User #${id}` : 'Unknown user');
  }
}
