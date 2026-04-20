import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { isNavPathAllowed } from '../../core/utils/nav-path-access';
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

  /** Lien profil dans le pied de page : même règle que l’entrée « My profile » du menu. */
  readonly showFooterProfile = computed(() =>
    isNavPathAllowed('/dashboard/profile', this.auth.currentUser()?.allowedNavPaths),
  );

  readonly navItems = computed<NavItem[]>(() => {
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
    const rest = core.filter((i) => i.href !== '/dashboard');
    if (dash) {
      return [dash, ...extras, ...rest];
    }
    return [...extras, ...rest];
  });

  logout(): void {
    this.auth.logout();
  }
}
