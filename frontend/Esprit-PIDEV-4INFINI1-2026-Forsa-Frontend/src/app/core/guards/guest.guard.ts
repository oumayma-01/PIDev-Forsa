import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Allows access only when not authenticated; valid session → redirect to dashboard. */
export const guestGuard: CanMatchFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.getAccessToken()) {
    return true;
  }

  if (await auth.ensureSessionFromApi()) {
    return router.createUrlTree(['/dashboard']);
  }

  return true;
};
