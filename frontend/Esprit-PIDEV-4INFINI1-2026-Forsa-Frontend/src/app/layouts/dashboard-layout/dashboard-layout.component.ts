import { Component, OnInit, AfterViewInit, ElementRef, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { DashboardNavbarComponent } from '../dashboard-navbar/dashboard-navbar.component';
import { DashboardSidebarCmp } from '../dashboard-sidebar/dashboard-sidebar.component';
import { gsap } from 'gsap';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, DashboardSidebarCmp, DashboardNavbarComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.css',
})
export class DashboardLayoutComponent implements OnInit, AfterViewInit {
  private readonly auth = inject(AuthService);
  private readonly el = inject(ElementRef);
  readonly isClient = computed(() => {
    const roles = this.auth.currentUser()?.roles ?? [];
    const hasPrivilegedRole = roles.some(role => {
      const r = role.toUpperCase();
      return r === 'ROLE_ADMIN' || r === 'ADMIN' || r === 'ROLE_AGENT' || r === 'AGENT';
    });
    return !hasPrivilegedRole;
  });

  /**
   * Recharge {@link AuthService#currentUser} depuis l’API pour mettre à jour
   * {@code allowedNavPaths} (menus sidebar) après une modification des droits par l’admin.
   */
  ngOnInit(): void {
    this.auth.refreshCurrentUser().subscribe({
      error: () => {
        /* session déjà gérée par authGuard ; ignorer */
      },
    });
  }

  ngAfterViewInit(): void {
    const root = this.el.nativeElement as HTMLElement;

    // Animate sidebar entrance
    const sidebar = root.querySelector('app-dashboard-sidebar');
    if (sidebar) {
      gsap.from(sidebar, {
        x: -30,
        opacity: 0,
        duration: 0.5,
        ease: 'power3.out',
      });
    }

    // Animate navbar entrance
    const navbar = root.querySelector('app-dashboard-navbar');
    if (navbar) {
      gsap.from(navbar, {
        y: -20,
        opacity: 0,
        duration: 0.4,
        ease: 'power3.out',
        delay: 0.15,
      });
    }

    // Animate main content
    const content = root.querySelector('.shell__content');
    if (content) {
      gsap.from(content, {
        opacity: 0,
        y: 12,
        duration: 0.5,
        ease: 'power2.out',
        delay: 0.3,
      });
    }
  }
}
