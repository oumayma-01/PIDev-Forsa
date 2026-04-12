import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanMatchFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (await auth.ensureSessionFromApi()) {
    return true;
  }
  return router.createUrlTree(['/login']);
};
