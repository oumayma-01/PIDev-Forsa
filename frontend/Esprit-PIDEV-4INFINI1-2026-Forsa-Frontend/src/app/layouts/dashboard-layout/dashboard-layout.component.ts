import {
  Component,
  OnInit,
  AfterViewInit,
  ElementRef,
  computed,
  effect,
  inject,
  signal,
  HostListener,
  DestroyRef,
  NgZone,
  afterNextRender,
} from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/services/auth.service';
import { DashboardNavbarComponent } from '../dashboard-navbar/dashboard-navbar.component';
import { DashboardSidebarCmp } from '../dashboard-sidebar/dashboard-sidebar.component';
import { gsap } from 'gsap';

/** Must match `dashboard-sidebar` CSS media query. */
const DASHBOARD_NAV_NARROW_MQ = '(max-width: 1023px)';
const DASHBOARD_NAV_NARROW_MAX_PX = 1023;

function readNarrowViewport(): boolean {
  if (typeof window === 'undefined') {
    return false;
  }
  try {
    const mq = window.matchMedia(DASHBOARD_NAV_NARROW_MQ);
    if (typeof mq.matches === 'boolean') {
      return mq.matches;
    }
  } catch {
    /* matchMedia can throw in locked-down contexts */
  }
  return window.innerWidth <= DASHBOARD_NAV_NARROW_MAX_PX;
}

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
  private readonly router = inject(Router);
  private readonly doc = inject(DOCUMENT);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  /** Admin / agent: viewport where sidebar becomes an off-canvas drawer. */
  readonly isNarrowView = signal(readNarrowViewport());
  readonly mobileDrawerOpen = signal(false);

  readonly isClient = computed(() => {
    const roles = this.auth.currentUser()?.roles ?? [];
    const hasPrivilegedRole = roles.some(role => {
      const r = role.toUpperCase();
      return r === 'ROLE_ADMIN' || r === 'ADMIN' || r === 'ROLE_AGENT' || r === 'AGENT';
    });
    return !hasPrivilegedRole;
  });

  /** Narrow viewports: hamburger opens the same off-canvas nav drawer (admin sidebar / client menu). */
  readonly showMobileNavToggle = computed(() => this.isNarrowView());

  constructor() {
    // Use matchMedia + change (not only CDK BreakpointObserver): some environments emit a
    // wrong first value on subscribe, or run outside NgZone — both break mobile after refresh.
    if (typeof window !== 'undefined' && typeof window.matchMedia === 'function') {
      const mq = window.matchMedia(DASHBOARD_NAV_NARROW_MQ);
      const applyFromMq = (): void => {
        this.zone.run(() => {
          const narrow = mq.matches || window.innerWidth <= DASHBOARD_NAV_NARROW_MAX_PX;
          this.isNarrowView.set(narrow);
          if (!narrow) {
            this.mobileDrawerOpen.set(false);
          }
        });
      };
      applyFromMq();
      const onMqChange = (): void => applyFromMq();
      if (typeof mq.addEventListener === 'function') {
        mq.addEventListener('change', onMqChange);
        this.destroyRef.onDestroy(() => mq.removeEventListener('change', onMqChange));
      } else {
        mq.addListener(onMqChange);
        this.destroyRef.onDestroy(() => mq.removeListener(onMqChange));
      }
    }

    afterNextRender(() => {
      this.zone.run(() => {
        const narrow = readNarrowViewport();
        this.isNarrowView.set(narrow);
        if (!narrow) {
          this.mobileDrawerOpen.set(false);
        }
      });
    });

    this.router.events
      .pipe(
        filter((e): e is NavigationEnd => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.mobileDrawerOpen.set(false));

    effect(() => {
      const lock = this.mobileDrawerOpen() && this.isNarrowView();
      this.doc.body.classList.toggle('forsa-scroll-lock', lock);
    });
  }

  toggleMobileDrawer(): void {
    this.mobileDrawerOpen.update((open) => !open);
  }

  closeMobileDrawer(): void {
    this.mobileDrawerOpen.set(false);
  }

  @HostListener('document:keydown.escape')
  onEscapeCloseDrawer(): void {
    if (this.mobileDrawerOpen() && this.isNarrowView()) {
      this.closeMobileDrawer();
    }
  }

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
    const isNarrow = readNarrowViewport();

    // Animate sidebar entrance (desktop only; mobile drawer uses CSS slide-in)
    const sidebar = root.querySelector('app-dashboard-sidebar');
    if (sidebar && !isNarrow) {
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
        clearProps: 'opacity',
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
