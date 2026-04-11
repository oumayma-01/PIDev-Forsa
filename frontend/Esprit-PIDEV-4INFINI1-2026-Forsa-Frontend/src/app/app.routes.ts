import { Routes } from '@angular/router';
import { RiskAnalysisComponent } from './features/ai/risk-analysis/risk-analysis.component';
import { LoginComponent } from './features/auth/login/login.component';
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
  { path: '', component: LandingPageComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'dashboard',
    component: DashboardLayoutComponent,
    children: [
      { path: '', component: DashboardHomeComponent },
      { path: 'credit', component: CreditListComponent },
      { path: 'wallet', component: WalletOverviewComponent },
      { path: 'insurance', component: InsuranceProductsComponent },
      { path: 'feedback', component: FeedbackListComponent },
      { path: 'partenariat', component: PartenariatListComponent },
      { path: 'scoring', component: ScoringWorkbenchComponent },
      { path: 'ai', component: RiskAnalysisComponent },
    ],
  },
  { path: '**', redirectTo: '', pathMatch: 'full' },
];
