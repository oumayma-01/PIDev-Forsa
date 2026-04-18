import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ComplaintService } from '../../../core/data/complaint.service';
import { ComplaintBackend, Category, Priority } from '../../../core/models/forsa.models';
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

  categories: Category[] = ['TECHNICAL', 'FINANCE', 'SUPPORT', 'FRAUD', 'ACCOUNT', 'CREDIT', 'OTHER'];
  priorities: Priority[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];

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
      this.complaintService.getById(+id).subscribe({
        next: (data: ComplaintBackend) => {
          this.complaint = data;
        },
        error: () => {
          this.error = 'Error loading complaint';
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

  save(): void {
    this.loading = true;
    this.error = '';

    if (this.isEditMode) {
      this.complaintService.update(this.complaint).subscribe({
        next: () => this.router.navigate(['/dashboard/feedback']),
        error: () => {
          this.error = 'Error updating complaint';
          this.loading = false;
        },
      });
    } else {
      const action = this.useAI
        ? this.complaintService.addWithAI(this.complaint)
        : this.complaintService.add(this.complaint);

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
