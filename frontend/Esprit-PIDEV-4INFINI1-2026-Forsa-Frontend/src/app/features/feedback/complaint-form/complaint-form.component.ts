import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, of } from 'rxjs';
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
    const id: string | null = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      const roles = this.auth.currentUser()?.roles ?? [];
      const isClient = roles.some(r => r === 'ROLE_CLIENT' || r === 'CLIENT');

      if (isClient) {
        // CLIENT: use getMyComplaints() and find by id
        this.complaintService.getMyComplaints().subscribe({
          next: (data: any) => {
            const payload = data?.data ?? data?.result ?? data?.content ?? data;
            const list = Array.isArray(payload) ? payload : [];
            const found = list.find((c: any) => Number(c.id) === +id!);
            if (found) {
              this.complaint = {
                id: found.id,
                subject: found.subject ?? '',
                description: found.description ?? '',
                category: found.category ?? 'OTHER',
                priority: found.priority ?? 'MEDIUM',
                status: found.status ?? 'OPEN',
              };
              this.complaintId = found.id;
              this.complaintService.getById(+id).pipe(
                catchError(() => of(this.complaint))
              ).subscribe((detail: any) => {
                const detailPayload = detail?.data ?? detail?.result ?? detail;
                this.complaint = {
                  id: detailPayload?.id ?? this.complaint.id,
                  subject: detailPayload?.subject ?? this.complaint.subject ?? '',
                  description: detailPayload?.description ?? this.complaint.description ?? '',
                  category: detailPayload?.category ?? this.complaint.category ?? 'OTHER',
                  priority: detailPayload?.priority ?? this.complaint.priority ?? 'MEDIUM',
                  status: detailPayload?.status ?? this.complaint.status ?? 'OPEN',
                };
                this.complaintId = this.complaint.id;
              });
            } else {
              this.error = 'Complaint not found.';
            }
          },
          error: () => {
            this.error = 'Error loading complaint';
          }
        });
      } else {
        // ADMIN/AGENT: use getById()
        this.complaintService.getById(+id).subscribe({
          next: (data: any) => {
            const payload = data?.data ?? data?.result ?? data;
            this.complaint = payload;
          },
          error: () => this.error = 'Error loading complaint'
        });
      }
    }
  }

  get isAdmin(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN') || roles.includes('ADMIN');
  }

  get isAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_AGENT') || roles.includes('AGENT');
  }

  get isAdminOrAgent(): boolean {
    return this.isAdmin || this.isAgent;
  }

  get isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.some(r => r === 'ROLE_CLIENT' || r === 'CLIENT');
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
    const currentUser = this.auth.currentUser();
    const payload: any = this.isClient && !this.isEditMode
      ? {
          subject: this.complaint.subject,
          description: this.complaint.description,
          category: 'OTHER',
          priority: 'MEDIUM',
          status: 'OPEN',
          user: currentUser?.id ? { id: currentUser.id } : undefined,
        }
      : this.complaint;

    if (this.isEditMode) {
      if (this.isClient && this.isEditMode) {
        const idToUse = Number(this.route.snapshot.paramMap.get('id'));
        const clientEditPayload: any = {
          id: idToUse,
          subject: this.complaint.subject?.trim(),
          description: this.complaint.description?.trim(),
          category: this.complaint.category ?? 'OTHER',
          priority: this.complaint.priority ?? 'MEDIUM',
          status: this.complaint.status ?? 'OPEN',
        };
        console.log('[ComplaintForm] client update payload:', clientEditPayload);
        this.complaintService.update(clientEditPayload).subscribe({
          next: () => this.router.navigate(['/dashboard/feedback']),
          error: (err) => {
            console.error('[ComplaintForm] update error:', err);
            this.error = 'Error updating complaint (status: ' + (err?.status ?? 'unknown') + ')';
            this.loading = false;
          },
        });
        return;
      }

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
      console.log('[ComplaintForm] submit payload:', payload);

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
