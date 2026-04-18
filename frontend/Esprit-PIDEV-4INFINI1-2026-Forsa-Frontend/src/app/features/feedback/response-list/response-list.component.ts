import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ResponseService } from '../../../core/data/response.service';
import { ComplaintResponse } from '../../../core/models/forsa.models';
import { ForsaBadgeComponent } from '../../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';

@Component({
  selector: 'app-response-list',
  standalone: true,
  imports: [CommonModule, ForsaBadgeComponent, ForsaButtonComponent, ForsaCardComponent, ForsaIconComponent],
  templateUrl: './response-list.component.html',
  styleUrl: './response-list.component.css',
})
export class ResponseListComponent implements OnInit {
  items: ComplaintResponse[] = [];
  loading = false;
  error = '';

  constructor(
    private responseService: ResponseService,
    private router: Router,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    this.loadResponses();
  }

  get isAdmin(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_ADMIN') ?? false;
  }

  get isAgent(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_AGENT') ?? false;
  }

  get isAdminOrAgent(): boolean {
    return this.isAdmin || this.isAgent;
  }

  loadResponses(): void {
    this.loading = true;
    this.responseService.getAll().subscribe({
      next: (data: ComplaintResponse[]) => {
        this.items = data;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error loading responses';
        this.loading = false;
      },
    });
  }

  goToAdd(): void {
    this.router.navigate(['/dashboard/feedback/response/add']);
  }

  goToEdit(id: number): void {
    this.router.navigate(['/dashboard/feedback/response', id]);
  }

  delete(id: number): void {
    if (confirm('Delete this response?')) {
      this.responseService.delete(id).subscribe({
        next: () => this.loadResponses(),
        error: () => (this.error = 'Error deleting response'),
      });
    }
  }

  improveWithAI(id: number): void {
    this.responseService.improveWithAI(id).subscribe({
      next: () => this.loadResponses(),
      error: () => (this.error = 'Error improving response'),
    });
  }

  statusTone(status: string): 'info' | 'warning' | 'success' | 'danger' {
    switch (status) {
      case 'PENDING':
        return 'warning';
      case 'PROCESSED':
        return 'info';
      case 'SENT':
        return 'success';
      case 'FAILED':
        return 'danger';
      default:
        return 'info';
    }
  }

  statusIcon(status: string): ForsaIconName {
    switch (status) {
      case 'PENDING':
        return 'clock';
      case 'PROCESSED':
        return 'check-circle-2';
      case 'SENT':
        return 'check-circle-2';
      case 'FAILED':
        return 'alert-circle';
      default:
        return 'alert-circle';
    }
  }
}
