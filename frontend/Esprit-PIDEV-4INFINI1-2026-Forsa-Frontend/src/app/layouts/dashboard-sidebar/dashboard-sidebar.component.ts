import { Component, computed, inject, type Signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { isNavPathAllowed } from '../../core/utils/nav-path-access';
import { AuthService as AuthSvc } from '../../core/services/auth.service';
import { ForsaLogoComponent as ForsaLogoCmp } from '../../shared/branding/forsa-logo.component';
import { ForsaIconComponent as ForsaIconCmp } from '../../shared/ui/forsa-icon/forsa-icon.component';

type NavIcon = import('../../shared/ui/forsa-icon/forsa-icon.types').ForsaIconName;

interface NavItem {
  label: string;
  href: string;
  icon: NavIcon;
}

@Component({
  selector: 'app-dashboard-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, ForsaLogoCmp, ForsaIconCmp],
  templateUrl: './dashboard-sidebar.component.html',
  styleUrl: './dashboard-sidebar.component.css',
})
export class DashboardSidebarComponent {
  private readonly auth = inject(AuthSvc);

  private readonly baseNav: NavItem[] = [
    { label: 'Dashboard', href: '/dashboard', icon: 'layout-dashboard' },
    { label: 'My profile', href: '/dashboard/profile', icon: 'user-circle' },
    { label: 'Credit Management', href: '/dashboard/credit', icon: 'credit-card' },
    { label: 'Digital Wallet', href: '/dashboard/wallet', icon: 'wallet' },
    { label: 'Insurance', href: '/dashboard/insurance', icon: 'shield-check' },
    { label: 'Partnerships', href: '/dashboard/partenariat', icon: 'users' },
    { label: 'Credit scoring', href: '/dashboard/scoring', icon: 'sparkles' },
    { label: 'Feedback', href: '/dashboard/feedback', icon: 'message-square' },
    { label: 'AI Risk Analysis', href: '/dashboard/ai', icon: 'bar-chart-3' },
  ];

  /** Footer profile link uses the same rule as the "My profile" nav entry. */
  readonly footerProfileVisible: Signal<boolean> = computed(() => {
    return isNavPathAllowed('/dashboard/profile', this.auth.currentUser()?.allowedNavPaths);
  });

  readonly navItems: Signal<NavItem[]> = computed((): NavItem[] => {
    const paths = this.auth.currentUser()?.allowedNavPaths;
    const allow = (href: string) => isNavPathAllowed(href, paths);
    const core = this.baseNav.filter((item) => allow(item.href));

    const extras: NavItem[] = [];
    if (allow('/dashboard/users')) {
      extras.push({ label: 'User management', href: '/dashboard/users', icon: 'settings' });
    }
    if (allow('/dashboard/roles')) {
      extras.push({ label: 'Role management', href: '/dashboard/roles', icon: 'shield' });
    }

    const dash = core.find((i) => i.href === '/dashboard');
    const withoutDashboard = core.filter((i) => i.href !== '/dashboard');

    const leading: NavItem[] = [];
    if (dash !== undefined) {
      leading.push(dash);
    }
    return [...leading, ...extras, ...withoutDashboard];
  });

  logout(): void {
    this.auth.logout();
  }
}