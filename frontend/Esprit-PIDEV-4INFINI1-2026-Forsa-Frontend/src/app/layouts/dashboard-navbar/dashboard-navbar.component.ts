import { Component, DestroyRef, ElementRef, HostListener, ViewChild, computed, inject, input, signal } from '@angular/core';
import { OverlayModule } from '@angular/cdk/overlay';
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
import { CreditApiService } from '../../core/services/credit-api.service';
import { GlobalNotificationService } from '../../core/services/global-notification.service';
import { ComplaintNotificationService } from '../../core/data/complaint-notification.service';

interface NotificationItem {
  id: string;
  message: string;
  type: 'complaint' | 'gift' | 'insurance-warning' | 'insurance-critical';
  amount?: number;
  actionRoute?: string;
}

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
  imports: [RouterLink, RouterLinkActive, OverlayModule, ForsaLogoComponent, ForsaInputDirective, ForsaBadgeComponent, ForsaIconComponent],
  templateUrl: './dashboard-navbar.component.html',
  styleUrl: './dashboard-navbar.component.css',
})
export class DashboardNavbarComponent {
  @ViewChild('notificationButton', { read: ElementRef }) private notificationButtonRef?: ElementRef<HTMLButtonElement>;
  readonly showSidebarItems = input<boolean>(false);
  readonly auth = inject(AuthService);
  private readonly profileApi = inject(ProfileService);
  private readonly creditApi = inject(CreditApiService);
  private readonly globalNotifService = inject(GlobalNotificationService);
  private readonly complaintNotificationService = inject(ComplaintNotificationService);
  private readonly destroyRef = inject(DestroyRef);
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
  readonly localNotifications = signal<NotificationItem[]>([]);
  readonly notifications = computed(() => {
    const global = this.globalNotifService.notifications().map(n => ({
      id: n.id,
      message: n.message,
      type: n.type as any, // mapping types
      actionRoute: n.actionRoute
    }));
    return [...this.localNotifications(), ...global];
  });
  readonly responseNotificationCount = computed(() => this.notifications().length);
  readonly nonComplaintNotificationCount = computed(
    () => this.notifications().filter((n) => n.type !== 'complaint').length,
  );
  readonly notificationMenuOpen = signal(false);
  private pollId: ReturnType<typeof setInterval> | null = null;

  private readonly baseNav: NavItem[] = [
    { label: 'Home', href: '/dashboard', icon: 'layout-dashboard' },
    { label: 'My profile', href: '/dashboard/profile', icon: 'user-circle' },
    { label: 'Credit Management', href: '/dashboard/credit', icon: 'credit-card' },
    { label: 'Digital Wallet', href: '/dashboard/wallet', icon: 'wallet' },
    { label: 'Insurance', href: '/dashboard/insurance', icon: 'shield-check' },
    { label: 'Partnerships', href: '/dashboard/partenariat', icon: 'users' },
    { label: 'My AI score', href: '/dashboard/ai-score', icon: 'brain' },
    { label: 'Feedback', href: '/dashboard/feedback', icon: 'message-square' },
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
    if (allow('/dashboard/scoring')) {
      extras.push({ label: 'Client scores', href: '/dashboard/scoring', icon: 'brain' });
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
    this.destroyRef.onDestroy(() => {
      this.revokeCustomAvatar();
      if (this.pollId) {
        clearInterval(this.pollId);
      }
    });

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

    if (this.isClient()) {
      this.loadComplaintNotifications();
    }
    this.startGiftPolling();
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

  private isClient(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('CLIENT') || roles.includes('ROLE_CLIENT');
  }

  private startGiftPolling(): void {
    this.pollGifts();
    this.pollId = setInterval(() => this.pollGifts(), 10000);
  }

  private pollGifts(): void {
    if (!this.isClient()) return;
    this.creditApi.consumeMyGiftAwardNotification().subscribe({
      next: (res) => {
        if (res?.show) {
          const amount = res.amount ?? 500;
          const msg = `Congratulations! You earned a loyalty gift of ${amount} DT.`;
          this.localNotifications.update(list => [{
            id: `gift-${Date.now()}`,
            message: msg,
            type: 'gift',
            amount: amount
          }, ...list]);
        }
      },
      error: () => {}
    });
  }

  toggleNotificationMenu(event: MouseEvent): void {
    event.stopPropagation();
    if (this.isClient()) {
      this.loadComplaintNotifications();
    }
    this.notificationMenuOpen.update((open) => {
      return !open;
    });
  }

  private loadComplaintNotifications(): void {
    this.complaintNotificationService.getMyNotifications().subscribe({
      next: (items) => {
        const mapped = (Array.isArray(items) ? items : []).map((n) => ({
          id: `complaint-${n.id}`,
          message: n.message || n.title || 'Complaint update',
          type: 'complaint' as const,
          actionRoute: n.complaint?.id ? `/dashboard/feedback/complaint/${n.complaint.id}` : '/dashboard/feedback',
        }));
        this.globalNotifService.notifications.set(mapped);
      },
      error: () => {
        this.globalNotifService.notifications.set([]);
      },
    });
  }

  closeNotificationMenu(): void {
    this.notificationMenuOpen.set(false);
  }

  clearAllNotifications(): void {
    this.localNotifications.set([]);
    this.globalNotifService.clearAll();
  }

  removeNotification(id: string, event?: MouseEvent): void {
    event?.stopPropagation();
    this.localNotifications.update(list => list.filter(n => n.id !== id));
    this.globalNotifService.removeNotification(id);
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
    if (!target?.closest('.notification-container')) {
      this.closeNotificationMenu();
    }
  }

}
