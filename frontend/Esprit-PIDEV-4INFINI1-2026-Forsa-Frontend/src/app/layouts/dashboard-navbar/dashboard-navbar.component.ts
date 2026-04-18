import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { ForsaLogoComponent } from '../../shared/branding/forsa-logo.component';
import { ForsaInputDirective } from '../../shared/directives/forsa-input.directive';
import { ForsaBadgeComponent } from '../../shared/ui/forsa-badge/forsa-badge.component';
import { ForsaIconComponent } from '../../shared/ui/forsa-icon/forsa-icon.component';

function formatSpringAuthority(authority: string): string {
  const raw = authority.replace(/^ROLE_/i, '').toLowerCase();
  return raw ? raw.charAt(0).toUpperCase() + raw.slice(1) : authority;
}

@Component({
  selector: 'app-dashboard-navbar',
  standalone: true,
  imports: [RouterLink, ForsaLogoComponent, ForsaInputDirective, ForsaBadgeComponent, ForsaIconComponent],
  templateUrl: './dashboard-navbar.component.html',
  styleUrl: './dashboard-navbar.component.css',
})
export class DashboardNavbarComponent {
  readonly auth = inject(AuthService);
  private readonly profileApi = inject(ProfileService);

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
}
