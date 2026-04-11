import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { DashboardNavbarComponent } from '../dashboard-navbar/dashboard-navbar.component';
import { DashboardSidebarComponent } from '../dashboard-sidebar/dashboard-sidebar.component';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterOutlet, DashboardSidebarComponent, DashboardNavbarComponent],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.css',
})
export class DashboardLayoutComponent {}
