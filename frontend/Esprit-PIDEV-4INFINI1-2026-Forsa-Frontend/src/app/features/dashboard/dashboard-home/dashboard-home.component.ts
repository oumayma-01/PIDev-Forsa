import { Component, OnInit, inject, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { UserAdminService } from '../../../core/services/user-admin.service';
import { UserDashboardOverview } from '../../../core/models/user-admin.model';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [ForsaCardComponent, ForsaIconComponent, RouterLink, CommonModule],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.css',
})
export class DashboardHomeComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly userAdminService = inject(UserAdminService);

  readonly stats = signal<UserDashboardOverview | null>(null);

  get isAdminOrAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_AGENT');
  }

  ngOnInit(): void {
    if (this.isAdminOrAgent) {
      this.userAdminService.getDashboardOverview().subscribe({
        next: (res) => this.stats.set(res),
        error: () => console.error('Could not load dashboard stats'),
      });
    }
  }
}
