import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanMatchFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!(await auth.ensureSessionFromApi())) {
    return router.createUrlTree(['/login']);
  }
  const roles = auth.currentUser()?.roles ?? [];
  if (roles.includes('ROLE_ADMIN')) {
    return true;
  }
  return router.createUrlTree(['/dashboard']);
};
