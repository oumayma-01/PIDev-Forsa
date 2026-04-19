import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Allows access only for ADMIN and AGENT roles.
 * Clients are redirected to the insurance front-office.
 */
export const backOfficeGuard: CanMatchFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  // Ensure we have the user loaded
  if (!auth.currentUser()) {
    await auth.ensureSessionFromApi();
  }

  const user = auth.currentUser();
  if (!user) {
    return router.createUrlTree(['/login']);
  }

  const isClient = user.roles.includes('ROLE_CLIENT');
  if (isClient) {
    return router.createUrlTree(['/dashboard/insurance/client']);
  }

  return true;
};

/**
 * Allows access only for CLIENT role.
 * Admins/Agents are redirected to the back-office hub.
 */
export const frontOfficeGuard: CanMatchFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.currentUser()) {
    await auth.ensureSessionFromApi();
  }

  const user = auth.currentUser();
  if (!user) {
    return router.createUrlTree(['/login']);
  }

  const isClient = user.roles.includes('ROLE_CLIENT');
  if (!isClient) {
    return router.createUrlTree(['/dashboard/insurance']);
  }

  return true;
};
