import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ForsaLogoComponent } from '../../shared/branding/forsa-logo.component';
import { ForsaInputDirective } from '../../shared/directives/forsa-input.directive';
import { ForsaBadgeComponent } from '../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaIconComponent } from '../../shared/ui/forsa-icon/forsa-icon.component';
import { MOCK_USER } from '../../core/data/mock-data';
import type { User } from '../../core/models/forsa.models';

@Component({
  selector: 'app-dashboard-navbar',
  standalone: true,
  imports: [FormsModule, RouterLink, ForsaLogoComponent, ForsaInputDirective, ForsaBadgeComponent, ForsaIconComponent],
  templateUrl: './dashboard-navbar.component.html',
  styleUrl: './dashboard-navbar.component.css',
})
export class DashboardNavbarComponent {
  readonly user = MOCK_USER;
  role: User['role'] = MOCK_USER.role;
  isDark = false;

  toggleDark(): void {
    this.isDark = !this.isDark;
    document.documentElement.classList.toggle('dark', this.isDark);
  }
}
