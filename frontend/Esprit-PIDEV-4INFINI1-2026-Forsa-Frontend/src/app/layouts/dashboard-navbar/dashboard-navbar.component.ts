import { Component, DestroyRef, HostListener, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { isNavPathAllowed } from '../../core/utils/nav-path-access';
import { ForsaLogoComponent } from '../../shared/branding/forsa-logo.component';
import { ForsaInputDirective } from '../../shared/directives/forsa-input.directive';
import { ForsaBadgeComponent } from '../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaIconComponent } from '../../shared/ui/forsa-icon/forsa-icon.component';
import type { ForsaIconName } from '../../shared/ui/forsa-icon/forsa-icon.types';

interface NavItem {
  label: string;
  href: string;
  icon: ForsaIconName;
}

function formatSpringAuthority(authority: string): string {
  const raw = authority.replace(/^ROLE_/i, '').toLowerCase();
  return raw ? raw.charAt(0).toUpperCase() + raw.slice(1) : authority;
}

@Component({
  selector: 'app-dashboard-navbar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, ForsaLogoComponent, ForsaInputDirective, ForsaBadgeComponent, ForsaIconComponent],
  templateUrl: './dashboard-navbar.component.html',
  styleUrl: './dashboard-navbar.component.css',
})
export class DashboardNavbarComponent {
  readonly showSidebarItems = input<boolean>(false);
  readonly auth = inject(AuthService);
  private readonly profileApi = inject(ProfileService);
  readonly profileMenuOpen = signal(false);

  /** Blob URL for uploaded profile photo; revoked on change or destroy. */
  private customAvatarRevoke: string | null = null;
  private readonly customAvatarUrl = signal<string | null>(null);

  readonly displayName = computed(() => this.auth.currentUser()?.username?.trim() || 'User');

  readonly rolesLabel = computed(() => {
    const roles = this.auth.currentUser()?.roles ?? [];
    if (!roles.length) {
      return 'User';
    }
    return roles.map(formatSpringAuthority).join(' · ');
  });

  readonly avatarSrc = computed(() => {
    const uploaded = this.customAvatarUrl();
    if (uploaded) {
      return uploaded;
    }
    const u = this.auth.currentUser();
    const seed = (u?.username ?? u?.email ?? 'user').trim() || 'user';
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(seed)}`;
  });

  private readonly baseNav: NavItem[] = [
    { label: 'Home', href: '/dashboard', icon: 'layout-dashboard' },
    { label: 'My profile', href: '/dashboard/profile', icon: 'user-circle' },
    { label: 'Credit Management', href: '/dashboard/credit', icon: 'credit-card' },
    { label: 'Digital Wallet', href: '/dashboard/wallet', icon: 'wallet' },
    { label: 'Insurance', href: '/dashboard/insurance', icon: 'shield-check' },
    { label: 'Partnerships', href: '/dashboard/partenariat', icon: 'users' },
    { label: 'Credit scoring', href: '/dashboard/scoring', icon: 'sparkles' },
    { label: 'My AI score', href: '/dashboard/ai-score', icon: 'brain' },
    { label: 'Feedback', href: '/dashboard/feedback', icon: 'message-square' },
    { label: 'AI Risk Analysis', href: '/dashboard/ai', icon: 'bar-chart-3' },
  ];

  readonly clientNavItems = computed(() => {
    const paths = this.auth.currentUser()?.allowedNavPaths;
    const allow = (href: string) => isNavPathAllowed(href, paths);
    const core = this.baseNav.filter((item) => allow(item.href) && item.href !== '/dashboard/profile');

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

  readonly profileLinkVisible = computed(() => {
    return isNavPathAllowed('/dashboard/profile', this.auth.currentUser()?.allowedNavPaths);
  });

  constructor() {
    inject(DestroyRef).onDestroy(() => this.revokeCustomAvatar());

    toObservable(this.auth.currentUser)
      .pipe(
        switchMap((u) => {
          if (!u?.hasProfileImage) {
            return of(null);
          }
          return this.profileApi.getAvatarBlob().pipe(
            map((blob) => (blob.size > 0 ? URL.createObjectURL(blob) : null)),
            catchError(() => of(null)),
          );
        }),
        takeUntilDestroyed(),
      )
      .subscribe((url) => {
        this.revokeCustomAvatar();
        if (url) {
          this.customAvatarRevoke = url;
          this.customAvatarUrl.set(url);
        }
      });
  }

  private revokeCustomAvatar(): void {
    if (this.customAvatarRevoke) {
      URL.revokeObjectURL(this.customAvatarRevoke);
      this.customAvatarRevoke = null;
    }
    this.customAvatarUrl.set(null);
  }

  isDark = false;

  toggleDark(): void {
    this.isDark = !this.isDark;
    document.documentElement.classList.toggle('dark', this.isDark);
  }

  toggleProfileMenu(event: MouseEvent): void {
    event.stopPropagation();
    this.profileMenuOpen.update((open) => !open);
  }

  closeProfileMenu(): void {
    this.profileMenuOpen.set(false);
  }

  logout(): void {
    this.closeProfileMenu();
    this.auth.logout();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement | null;
    if (!target?.closest('.profile-menu')) {
      this.closeProfileMenu();
    }
  }
}
