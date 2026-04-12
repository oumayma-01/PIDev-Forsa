import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ForsaLogoComponent } from '../../shared/branding/forsa-logo.component';
import { ForsaIconComponent } from '../../shared/ui/forsa-icon/forsa-icon.component';
import type { ForsaIconName } from '../../shared/ui/forsa-icon/forsa-icon.types';

interface NavItem {
  label: string;
  href: string;
  icon: ForsaIconName;
}

@Component({
  selector: 'app-dashboard-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, ForsaLogoComponent, ForsaIconComponent],
  templateUrl: './dashboard-sidebar.component.html',
  styleUrl: './dashboard-sidebar.component.css',
})
export class DashboardSidebarComponent {
  private readonly auth = inject(AuthService);

  readonly navItems: NavItem[] = [
    { label: 'Dashboard', href: '/dashboard', icon: 'layout-dashboard' },
    { label: 'Credit Management', href: '/dashboard/credit', icon: 'credit-card' },
    { label: 'Digital Wallet', href: '/dashboard/wallet', icon: 'wallet' },
    { label: 'Insurance', href: '/dashboard/insurance', icon: 'shield-check' },
    { label: 'Partnerships', href: '/dashboard/partenariat', icon: 'users' },
    { label: 'Credit scoring', href: '/dashboard/scoring', icon: 'sparkles' },
    { label: 'Feedback', href: '/dashboard/feedback', icon: 'message-square' },
    { label: 'AI Risk Analysis', href: '/dashboard/ai', icon: 'bar-chart-3' },
  ];

  logout(): void {
    this.auth.logout();
  }
}
