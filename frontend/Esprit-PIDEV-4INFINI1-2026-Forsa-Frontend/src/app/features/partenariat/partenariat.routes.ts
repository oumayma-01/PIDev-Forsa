import { Routes } from '@angular/router';
import { adminGuard } from '../../core/guards/admin.guard';
import { PartenariatListComponent } from './partenariat-list/partenariat-list.component';

export const partenariatRoutes: Routes = [
  { path: '', component: PartenariatListComponent },
  {
    path: 'new',
    canMatch: [adminGuard],
    loadComponent: () =>
      import('./partner-form/partner-form.component').then((m) => m.PartnerFormComponent),
  },
  {
    path: 'my-transactions',
    loadComponent: () =>
      import('./client-transactions/client-transactions.component').then(
        (m) => m.ClientTransactionsComponent,
      ),
  },
  {
    path: 'scan',
    loadComponent: () =>
      import('./qr-scan/qr-scan.component').then((m) => m.QrScanComponent),
  },
  {
    path: ':id/edit',
    canMatch: [adminGuard],
    loadComponent: () =>
      import('./partner-form/partner-form.component').then((m) => m.PartnerFormComponent),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./partner-detail/partner-detail.component').then((m) => m.PartnerDetailComponent),
  },
];
