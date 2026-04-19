import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { ComplaintBackend } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';
@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [CommonModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './feedback-list.component.html',
  styleUrl: './feedback-list.component.css',
})
export class FeedbackListComponent implements OnInit {

  items: ComplaintBackend[] = [];
  loading = false;
  error = '';

  constructor(
    private complaintService: ComplaintService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadComplaints();
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

  loadComplaints(): void {
    this.loading = true;
    this.complaintService.getAll().subscribe({
      next: (data: ComplaintBackend[]) => {
        this.items = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error loading complaints';
        this.loading = false;
      },
    });
  }

  goToAdd(): void {
    this.router.navigate(['/dashboard/feedback/complaint/add']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/dashboard/feedback/complaint', id]);
  }

  goToFeedback(): void {
    this.router.navigate(['/dashboard/feedback/feedback']);
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

  delete(id: number): void {
    if (confirm('Delete this complaint?')) {
      this.complaintService.delete(id).subscribe({
        next: () => this.loadComplaints(),
        error: () => (this.error = 'Error deleting complaint'),
      });
    }
  }

  close(id: number): void {
    this.complaintService.close(id).subscribe({
      next: () => this.loadComplaints(),
      error: () => (this.error = 'Error closing complaint'),
    });
  }

  statusIcon(status: string): ForsaIconName {
    switch (status) {
      case 'OPEN': return 'alert-circle';
      case 'IN_PROGRESS': return 'clock';
      case 'RESOLVED': return 'check-circle-2';
      case 'CLOSED': return 'alert-circle';
      case 'REJECTED': return 'alert-circle';
      default: return 'alert-circle';
    }
  }

  statusTone(status: string): 'info' | 'warning' | 'success' | 'danger' {
    switch (status) {
      case 'OPEN': return 'info';
      case 'IN_PROGRESS': return 'warning';
      case 'RESOLVED': return 'success';
      case 'CLOSED': return 'danger';
      case 'REJECTED': return 'danger';
      default: return 'info';
    }
  }

  priorityTone(priority: string): 'danger' | 'warning' | 'info' {
    switch (priority) {
      case 'CRITICAL': return 'danger';
      case 'HIGH': return 'danger';
      case 'MEDIUM': return 'warning';
      case 'LOW': return 'info';
      default: return 'info';
    }
  }
}
