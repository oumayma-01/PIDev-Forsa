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
      { path: 'insurance', loadChildren: () => import('./features/insurance/insurance.routes').then(m => m.insuranceRoutes) },
      {
        path: 'feedback',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/feedback/feedback-list/feedback-list.component').then((m) => m.FeedbackListComponent),
          },
          {
            path: 'complaint/add',
            loadComponent: () =>
              import('./features/feedback/complaint-form/complaint-form.component').then((m) => m.ComplaintFormComponent),
          },
          {
            path: 'complaint/:id',
            loadComponent: () =>
              import('./features/feedback/complaint-detail/complaint-detail.component').then((m) => m.ComplaintDetailComponent),
          },
          {
            path: 'complaint/:id/edit',
            loadComponent: () =>
              import('./features/feedback/complaint-form/complaint-form.component').then((m) => m.ComplaintFormComponent),
          },
          {
            path: 'feedback/add',
            loadComponent: () =>
              import('./features/feedback/feedback-form/feedback-form.component').then((m) => m.FeedbackFormComponent),
          },
          {
            path: 'feedback/:id',
            loadComponent: () =>
              import('./features/feedback/feedback-form/feedback-form.component').then((m) => m.FeedbackFormComponent),
          },
          {
            path: 'responses',
            loadComponent: () =>
              import('./features/feedback/response-list/response-list.component').then((m) => m.ResponseListComponent),
          },
          {
            path: 'response/add',
            loadComponent: () =>
              import('./features/feedback/response-form/response-form.component').then((m) => m.ResponseFormComponent),
          },
          {
            path: 'response/:id',
            loadComponent: () =>
              import('./features/feedback/response-form/response-form.component').then((m) => m.ResponseFormComponent),
          },
          {
            path: 'chatbot',
            loadComponent: () =>
              import('./features/feedback/chatbot/chatbot.component').then((m) => m.ChatbotComponent),
          },
          {
            path: 'stats',
            canMatch: [adminGuard],
            loadComponent: () =>
              import('./features/feedback/feedback-stats/feedback-stats.component').then((m) => m.FeedbackStatsComponent),
          },
        ],
      },
      { path: 'partenariat', component: PartenariatListComponent },
      { path: 'scoring', component: ScoringWorkbenchComponent },
      { path: 'ai', component: RiskAnalysisComponent },
      {
        path: 'users',
        canMatch: [adminGuard],
        loadComponent: () =>
          import('./features/admin/user-management/user-management.component').then((m) => m.UserManagementComponent),
      },
      {
        path: 'roles',
        canMatch: [adminGuard],
        loadComponent: () =>
          import('./features/admin/role-management/role-management.component').then((m) => m.RoleManagementComponent),
      },
    ],
  },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];
