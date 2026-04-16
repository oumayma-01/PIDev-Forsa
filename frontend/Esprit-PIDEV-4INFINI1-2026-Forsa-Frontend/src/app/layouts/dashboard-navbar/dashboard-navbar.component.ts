import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
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

  readonly displayName = computed(() => this.auth.currentUser()?.username?.trim() || 'User');

  readonly rolesLabel = computed(() => {
    const roles = this.auth.currentUser()?.roles ?? [];
    if (!roles.length) {
      return 'User';
    }
    return roles.map(formatSpringAuthority).join(' · ');
  });

  readonly avatarSrc = computed(() => {
    const u = this.auth.currentUser();
    const seed = (u?.username ?? u?.email ?? 'user').trim() || 'user';
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${encodeURIComponent(seed)}`;
  });

  isDark = false;

  toggleDark(): void {
    this.isDark = !this.isDark;
    document.documentElement.classList.toggle('dark', this.isDark);
  }
}
