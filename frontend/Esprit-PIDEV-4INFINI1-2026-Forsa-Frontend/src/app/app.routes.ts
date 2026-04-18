import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { RiskAnalysisComponent } from './features/ai/risk-analysis/risk-analysis.component';
import { ForgotPasswordComponent } from './features/auth/forgot-password/forgot-password.component';
import { LoginComponent } from './features/auth/login/login.component';
import { OAuthSuccessComponent } from './features/auth/oauth-success/oauth-success.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { CreditListComponent } from './features/credit/credit-list/credit-list.component';
import { DashboardHomeComponent } from './features/dashboard/dashboard-home/dashboard-home.component';
import { FeedbackListComponent } from './features/feedback/feedback-list/feedback-list.component';
import { InsuranceProductsComponent } from './features/insurance/insurance-products/insurance-products.component';
import { LandingPageComponent } from './features/landing/landing-page.component';
import { WalletOverviewComponent } from './features/wallet/wallet-overview/wallet-overview.component';
import { PartenariatListComponent } from './features/partenariat/partenariat-list/partenariat-list.component';
import { ScoringWorkbenchComponent } from './features/scoring/scoring-workbench/scoring-workbench.component';
import { DashboardLayoutComponent } from './layouts/dashboard-layout/dashboard-layout.component';

export const routes: Routes = [
  /** `pathMatch: 'full'` avoids the empty path acting as a prefix for every URL. */
  { path: '', pathMatch: 'full', component: LandingPageComponent, canMatch: [guestGuard] },
  { path: 'login', component: LoginComponent, canMatch: [guestGuard] },
  { path: 'register', component: RegisterComponent, canMatch: [guestGuard] },
  { path: 'oauth-success', component: OAuthSuccessComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent, canMatch: [guestGuard] },
  {
    path: 'pages/validateUser/:id',
    loadComponent: () =>
      import('./features/auth/validate-user/validate-user.component').then((m) => m.ValidateUserComponent),
  },
  {
    path: 'pages/changepassword/:token/:email',
    loadComponent: () =>
      import('./features/auth/change-password/change-password.component').then((m) => m.ChangePasswordComponent),
  },
  {
    path: 'dashboard',
    component: DashboardLayoutComponent,
    canMatch: [authGuard],
    children: [
      { path: '', component: DashboardHomeComponent },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/profile/profile-page/profile-page.component').then((m) => m.ProfilePageComponent),
      },
      { path: 'credit', component: CreditListComponent },
      { path: 'wallet', component: WalletOverviewComponent },
      { path: 'insurance', component: InsuranceProductsComponent },
      { path: 'feedback', component: FeedbackListComponent },
      { path: 'partenariat', component: PartenariatListComponent },
      { path: 'scoring', component: ScoringWorkbenchComponent },
      { path: 'ai', component: RiskAnalysisComponent },
      {
        path: 'users',
        canMatch: [adminGuard],
        loadComponent: () =>
          import('./features/admin/user-management/user-management.component').then((m) => m.UserManagementComponent),
      },
    ],
  },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];
