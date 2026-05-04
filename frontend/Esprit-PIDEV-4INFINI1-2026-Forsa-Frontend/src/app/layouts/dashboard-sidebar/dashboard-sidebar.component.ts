import { Component, Input, computed, inject, type Signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { isNavPathAllowed } from '../../core/utils/nav-path-access';
import { AuthService as ForsaAuth } from '../../core/services/auth.service';
import { ForsaLogoComponent as ForsaSidebarLogo } from '../../shared/branding/forsa-logo.component';
import { ForsaIconComponent as ForsaSidebarIcon } from '../../shared/ui/forsa-icon/forsa-icon.component';
import type { ForsaIconName as SidebarNavIcon } from '../../shared/ui/forsa-icon/forsa-icon.types';

interface NavItem {
  label: string;
  href: string;
  icon: SidebarNavIcon;
}

@Component({
  selector: 'app-dashboard-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, ForsaSidebarLogo, ForsaSidebarIcon],
  templateUrl: './dashboard-sidebar.component.html',
  styleUrl: './dashboard-sidebar.component.css',
  host: {
    '[class.sidebar-host--drawer-open]': 'drawerOpen',
  },
})
export class DashboardSidebarCmp {
  private readonly auth = inject(ForsaAuth);

  /** When true on narrow viewports, the sidebar panel is visible (off-canvas drawer). */
  @Input() drawerOpen = false;

  private readonly baseNav: NavItem[] = [
    { label: 'Home', href: '/dashboard', icon: 'layout-dashboard' },
    { label: 'My profile', href: '/dashboard/profile', icon: 'user-circle' },
    { label: 'Credit Management', href: '/dashboard/credit', icon: 'credit-card' },
    { label: 'Digital Wallet', href: '/dashboard/wallet', icon: 'wallet' },
    { label: 'Insurance', href: '/dashboard/insurance', icon: 'shield-check' },
    { label: 'Partnerships', href: '/dashboard/partenariat', icon: 'users' },
    { label: 'My score', href: '/dashboard/ai-score', icon: 'brain' },
    { label: 'Feedback', href: '/dashboard/feedback', icon: 'message-square' },
  ];

  /** Footer profile link uses the same rule as the "My profile" nav entry. */
  readonly footerProfileVisible: Signal<boolean> = computed(() => {
    return isNavPathAllowed('/dashboard/profile', this.auth.currentUser()?.allowedNavPaths);
  });

  readonly navItems: Signal<NavItem[]> = computed((): NavItem[] => {
    const paths = this.auth.currentUser()?.allowedNavPaths;
    const roles = this.auth.currentUser()?.roles ?? [];
    const isAdmin = roles.includes('ROLE_ADMIN');
    const isAgent = roles.includes('ROLE_AGENT');
    const isClient = roles.includes('ROLE_CLIENT') || roles.includes('CLIENT');
    const allow = (href: string) => isNavPathAllowed(href, paths);
    const core = this.baseNav.filter((item) => {
      if (!allow(item.href)) return false;
      if (isClient && item.href === '/dashboard/profile') return false;
      if (item.href === '/dashboard/ai-score' && isAdmin) return false;
      return true;
    });

    const extras: NavItem[] = [];
    if (allow('/dashboard/users')) {
      extras.push({ label: 'User management', href: '/dashboard/users', icon: 'settings' });
    }
    if (allow('/dashboard/roles')) {
      extras.push({ label: 'Role management', href: '/dashboard/roles', icon: 'shield' });
    }
    if (isAdmin && allow('/dashboard/scoring')) {
      extras.push({ label: 'Client scores', href: '/dashboard/scoring', icon: 'brain' });
    }
    if (isAgent && allow('/dashboard/feedback/responses')) {
      extras.push({ label: 'Response management', href: '/dashboard/feedback/responses', icon: 'send' });
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
