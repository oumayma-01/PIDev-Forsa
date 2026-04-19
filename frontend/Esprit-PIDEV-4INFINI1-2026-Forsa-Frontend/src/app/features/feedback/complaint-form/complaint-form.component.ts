import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { ComplaintBackend, Category, Priority, ComplaintStatus } from '../../../core/models/forsa.models';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';

@Component({
  selector: 'app-complaint-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
  ],
  templateUrl: './complaint-form.component.html',
  styleUrl: './complaint-form.component.css',
})
export class ComplaintFormComponent implements OnInit {
  complaint: ComplaintBackend = {
    subject: '',
    description: '',
    category: 'OTHER',
    priority: 'MEDIUM',
    status: 'OPEN',
  };

  isEditMode = false;
  useAI = false;
  loading = false;
  error = '';
  complaintId?: number;
  validationErrors: string[] = [];

  categories: Category[] = ['TECHNICAL', 'FINANCE', 'SUPPORT', 'FRAUD', 'ACCOUNT', 'CREDIT', 'OTHER'];
  priorities: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  statuses: ComplaintStatus[] = ['OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED', 'REJECTED'];

  constructor(
    private complaintService: ComplaintService,
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.complaintId = +id;
      this.loading = true;
      this.complaintService.getById(+id).subscribe({
        next: (data: ComplaintBackend) => {
          this.complaint = data;
          this.loading = false;
        },
        error: () => {
          this.error = 'Error loading complaint';
          this.loading = false;
        },
      });
    }
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

  get isClient(): boolean {
    return this.auth.currentUser()?.roles?.includes('ROLE_CLIENT') ?? false;
  }

  private validate(): boolean {
    this.validationErrors = [];
    const subjectLength = (this.complaint.subject ?? '').trim().length;
    const descriptionLength = (this.complaint.description ?? '').trim().length;
    if (subjectLength < 5 || subjectLength > 200) {
      this.validationErrors.push('Subject must be between 5 and 200 characters.');
    }
    if (descriptionLength < 10 || descriptionLength > 1000) {
      this.validationErrors.push('Description must be between 10 and 1000 characters.');
    }
    return this.validationErrors.length === 0;
  }

  save(): void {
    if (!this.validate()) {
      return;
    }
    this.loading = true;
    this.error = '';
    const payload: ComplaintBackend = this.isClient && !this.isEditMode
      ? {
          subject: this.complaint.subject,
          description: this.complaint.description,
          category: 'OTHER',
          priority: 'MEDIUM',
          status: 'OPEN',
        }
      : this.complaint;

    if (this.isEditMode) {
      this.complaintService.update({ ...payload, id: this.complaintId }).subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Error updating complaint';
          this.loading = false;
        },
      });
    } else {
      const action = this.useAI
        ? this.complaintService.addWithAI(payload)
        : this.complaintService.add(payload);

      action.subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Error submitting complaint';
          this.loading = false;
        },
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/dashboard/feedback']);
  }
}
