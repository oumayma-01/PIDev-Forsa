import { Routes } from '@angular/router';
import { backOfficeGuard, frontOfficeGuard } from '../../core/guards/role.guard';

export const insuranceRoutes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./insurance-hub/insurance-hub.component').then((m) => m.InsuranceHubComponent),
  },
  // ── Products (back-office: ADMIN / AGENT only) ───────────────────────────
  {
    path: 'products',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-products/pages/product-list/product-list.component').then(
        (m) => m.ProductListComponent,
      ),
  },
  {
    path: 'products/new',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-products/pages/product-form/product-form.component').then(
        (m) => m.ProductFormComponent,
      ),
  },
  {
    path: 'products/:id',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-products/pages/product-detail/product-detail.component').then(
        (m) => m.ProductDetailComponent,
      ),
  },
  {
    path: 'products/:id/edit',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-products/pages/product-form/product-form.component').then(
        (m) => m.ProductFormComponent,
      ),
  },
  // ── Policies (back-office: ADMIN / AGENT only) ───────────────────────────
  {
    path: 'policies',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-premium/pages/policy-list/policy-list.component').then(
        (m) => m.PolicyListComponent,
      ),
  },
  {
    path: 'policies/apply',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-premium/pages/policy-form/policy-form.component').then(
        (m) => m.PolicyFormComponent,
      ),
  },
  {
    path: 'policies/:id',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-premium/pages/policy-detail/policy-detail.component').then(
        (m) => m.PolicyDetailComponent,
      ),
  },
  // ── Claims (back-office: ADMIN / AGENT only) ─────────────────────────────
  {
    path: 'claims',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-claims/pages/claim-list/claim-list.component').then(
        (m) => m.ClaimListComponent,
      ),
  },
  {
    path: 'claims/new',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-claims/pages/claim-form/claim-form.component').then(
        (m) => m.ClaimFormComponent,
      ),
  },
  {
    path: 'claims/:id',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-claims/pages/claim-detail/claim-detail.component').then(
        (m) => m.ClaimDetailComponent,
      ),
  },
  {
    path: 'claims/:id/edit',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import('./insurance-claims/pages/claim-form/claim-form.component').then(
        (m) => m.ClaimFormComponent,
      ),
  },
  // ── Premium Payments (back-office: ADMIN / AGENT only) ───────────────────
  {
    path: 'payments',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import(
        './insurance-premium-payements/pages/premium-payement-list/premium-payement-list.component'
      ).then((m) => m.PremiumPayementListComponent),
  },
  {
    path: 'payments/new',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import(
        './insurance-premium-payements/pages/premium-payements-form/premium-payements-form.component'
      ).then((m) => m.PremiumPayementsFormComponent),
  },
  {
    path: 'payments/:id',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import(
        './insurance-premium-payements/pages/premium-payement-detail/premium-payement-detail.component'
      ).then((m) => m.PremiumPayementDetailComponent),
  },
  {
    path: 'payments/:id/edit',
    canMatch: [backOfficeGuard],
    loadComponent: () =>
      import(
        './insurance-premium-payements/pages/premium-payements-form/premium-payements-form.component'
      ).then((m) => m.PremiumPayementsFormComponent),
  },
  // ── Client Front Office (CLIENT role only) ───────────────────────────────
  {
    path: 'client',
    canMatch: [frontOfficeGuard],
    loadChildren: () =>
      import('./front-office/front-office.routes').then((m) => m.frontOfficeRoutes),
  },
];

