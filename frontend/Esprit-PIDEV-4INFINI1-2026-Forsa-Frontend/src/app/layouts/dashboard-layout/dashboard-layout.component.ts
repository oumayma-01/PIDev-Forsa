import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { DashboardNavbarComponent } from '../dashboard-navbar/dashboard-navbar.component';
import { DashboardSidebarCmp } from '../dashboard-sidebar/dashboard-sidebar.component';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, DashboardSidebarCmp, DashboardNavbarComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.css',
})
export class DashboardLayoutComponent implements OnInit {
  private readonly auth = inject(AuthService);

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
}
