import { Component, OnInit, AfterViewInit, ElementRef, computed, inject, signal } from '@angular/core';
import { AuthService } from '../../../core/services/auth.service';
import { UserAdminService } from '../../../core/services/user-admin.service';
import { UserDashboardOverview } from '../../../core/models/user-admin.model';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import type { ForsaIconName } from '../../../shared/ui/forsa-icon/forsa-icon.types';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { gsap } from 'gsap';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [ForsaCardComponent, ForsaIconComponent, RouterLink, CommonModule],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.css',
})
export class DashboardHomeComponent implements OnInit, AfterViewInit {
  private readonly auth = inject(AuthService);
  private readonly userAdminService = inject(UserAdminService);
  private readonly el = inject(ElementRef);

  readonly stats = signal<UserDashboardOverview | null>(null);
  readonly quickActions = computed(() => {
    if (this.isAdmin) {
      return [
        { label: 'User management', description: 'Manage accounts and activation status.', path: '/dashboard/users', icon: 'users' as ForsaIconName },
        { label: 'Role access', description: 'Control sidebar permissions per role.', path: '/dashboard/roles', icon: 'shield-check' as ForsaIconName },
        { label: 'Feedback queue', description: 'Review complaints and response workflow.', path: '/dashboard/feedback', icon: 'message-square' as ForsaIconName },
        { label: 'Client AI scores', description: 'Real-time AI scores for all clients.', path: '/dashboard/scoring', icon: 'brain' as ForsaIconName },
      ];
    }
    return [
      { label: 'Credit operations', description: 'Review and follow active credit requests.', path: '/dashboard/credit', icon: 'credit-card' as ForsaIconName },
      { label: 'Insurance operations', description: 'Handle policies, products, and claims.', path: '/dashboard/insurance', icon: 'shield' as ForsaIconName },
      { label: 'Feedback queue', description: 'Treat complaints and keep customers informed.', path: '/dashboard/feedback', icon: 'message-square' as ForsaIconName },
    ];
  });

  get isAdmin(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_ADMIN');
  }

  get isAgent(): boolean {
    const roles = this.auth.currentUser()?.roles ?? [];
    return roles.includes('ROLE_AGENT');
  }

  get isAdminOrAgent(): boolean {
    return this.isAdmin || this.isAgent;
  }

  get dashboardHeading(): string {
    return this.isAdmin ? 'Platform command center' : 'Agent operations workspace';
  }

  get dashboardSubheading(): string {
    return this.isAdmin
      ? 'Track activation quality and governance in one place.'
      : 'Monitor customer activity, prioritize operations, and keep service levels high.';
  }

  get roleBadgeLabel(): string {
    return this.isAdmin ? 'Administrator view' : 'Agent view';
  }

  percentOf(part: number, total: number): number {
    if (total <= 0) {
      return 0;
    }
    return Math.round((part / total) * 100);
  }

  activationRatePercent(s: UserDashboardOverview): number {
    const raw = Number(s.activationRate ?? 0);
    if (Number.isFinite(raw) && raw > 0) {
      if (raw <= 1) {
        return Number((raw * 100).toFixed(1));
      }
      if (raw <= 100) {
        return Number(raw.toFixed(1));
      }
    }

    if (s.totalUsers <= 0) {
      return 0;
    }
    return Number(((s.activeUsers / s.totalUsers) * 100).toFixed(1));
  }

  ngOnInit(): void {
    if (this.isAdminOrAgent) {
      this.userAdminService.getDashboardOverview().subscribe({
        next: (res) => this.stats.set(res),
        error: () => console.error('Could not load dashboard stats'),
      });
    }
  }

  ngAfterViewInit(): void {
    const root = this.el.nativeElement as HTMLElement;

    // Staggered card entrance animation
    const cards = root.querySelectorAll('app-forsa-card');
    if (cards.length) {
      gsap.from(Array.from(cards), {
        y: 24,
        opacity: 0,
        duration: 0.5,
        stagger: 0.1,
        ease: 'power3.out',
        delay: 0.2,
      });
    }

    // Animate heading
    const heading = root.querySelector('.page-head');
    if (heading) {
      gsap.from(heading, {
        y: -15,
        opacity: 0,
        duration: 0.4,
        ease: 'power2.out',
      });
    }
  }
}
