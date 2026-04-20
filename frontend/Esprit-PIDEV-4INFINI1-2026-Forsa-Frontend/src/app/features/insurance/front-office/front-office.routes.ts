import { Routes } from '@angular/router';

export const frontOfficeRoutes: Routes = [
  {
    path: '',
    redirectTo: '../', // Redirect to the parent insurance hub
    pathMatch: 'full'
  },
  {
    path: 'catalog',
    loadComponent: () =>
      import('./client-product-catalog/client-product-catalog.component').then(
        (m) => m.ClientProductCatalogComponent,
      ),
  },
  {
    path: 'apply/:id',
    loadComponent: () =>
      import('./client-policy-application/client-policy-application.component').then(
        (m) => m.ClientPolicyApplicationComponent,
      ),
  },
  {
    path: 'my-policies',
    loadComponent: () =>
      import('./client-my-policies/client-my-policies.component').then(
        (m) => m.ClientMyPoliciesComponent,
      ),
  },
  {
    path: 'my-claims',
    loadComponent: () =>
      import('./client-my-claims/client-my-claims.component').then(
        (m) => m.ClientMyClaimsComponent,
      ),
  },
  {
    path: 'my-payments',
    loadComponent: () =>
      import('./client-my-payments/client-my-payments.component').then(
        (m) => m.ClientMyPaymentsComponent,
      ),
  },
  {
    path: 'file-claim/:policyId',
    loadComponent: () =>
      import('./client-claim-filing/client-claim-filing.component').then(
        (m) => m.ClientClaimFilingComponent,
      ),
  }
];
