import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ResponseService } from '../../../core/data/response.service';
import { ComplaintResponse, ResponseStatus } from '../../../core/models/forsa.models';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';

@Component({
  selector: 'app-response-form',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
  ],
  templateUrl: './response-form.component.html',
  styleUrl: './response-form.component.css',
})
export class ResponseFormComponent implements OnInit {
  response: ComplaintResponse = {
    message: '',
    responderRole: '',
    responderName: '',
    responseStatus: 'PENDING',
  };

  isEditMode = false;
  loading = false;
  error = '';
  complaintId?: number;

  statuses: ResponseStatus[] = ['PENDING', 'PROCESSED', 'SENT', 'FAILED'];

  constructor(
    private responseService: ResponseService,
    private router: Router,
    private route: ActivatedRoute,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    if (!this.isAdminOrAgent) {
      this.router.navigate(['/dashboard/feedback']);
      return;
    }
    const queryComplaintId = Number(this.route.snapshot.queryParamMap.get('complaintId'));
    if (queryComplaintId) {
      this.complaintId = queryComplaintId;
    }
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.loading = true;
      this.responseService.getById(+id).subscribe({
        next: (data: ComplaintResponse) => {
          this.response = data;
          this.loading = false;
        },
        error: () => {
          this.error = 'Error loading response';
          this.loading = false;
        },
      });
    }

    // Populate responder info from current user
    const user = this.auth.currentUser();
    if (user && !this.isEditMode) {
      this.response.responderName = user.username || '';
      this.response.responderRole = user.roles?.[0]?.replace('ROLE_', '') || 'AGENT';
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

  save(): void {
    if ((this.response.message ?? '').trim().length < 10) {
      this.error = 'Message must be at least 10 characters.';
      return;
    }
    if ((this.response.responderRole ?? '').trim().length === 0 || (this.response.responderName ?? '').trim().length < 2) {
      this.error = 'Responder name and role are required.';
      return;
    }
    this.loading = true;
    this.error = '';

    if (this.isEditMode) {
      this.responseService.update(this.response).subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Error updating response';
          this.loading = false;
        },
      });
    } else {
      const payload: ComplaintResponse = {
        ...this.response,
        complaint: this.complaintId ? { id: this.complaintId, subject: '', description: '' } : this.response.complaint,
      };
      this.responseService.add(payload).subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Error creating response';
          this.loading = false;
        },
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/dashboard/feedback']);
  }
}
