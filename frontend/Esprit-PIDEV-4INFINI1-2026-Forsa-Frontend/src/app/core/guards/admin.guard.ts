import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { isNavPathAllowed } from '../utils/nav-path-access';
import { AuthService } from '../services/auth.service';

/**
 * - **Feedback stats** : réservé aux comptes {@code ROLE_ADMIN} (+ matrice si elle est renseignée).
 * - **User management / Role management** : tout utilisateur authentifié si {@code allowedNavPaths}
 *   contient le chemin (ex. client avec droits accordés dans la gestion des accès).
 */
export const adminGuard: CanMatchFn = async () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!(await auth.ensureSessionFromApi())) {
    return router.createUrlTree(['/login']);
  }

  const roles = auth.currentUser()?.roles ?? [];
  const paths = auth.currentUser()?.allowedNavPaths;
  const attempted = navigationTargetPath(router);

  if (attempted.startsWith('/dashboard/feedback/stats')) {
    if (!roles.includes('ROLE_ADMIN')) {
      return router.createUrlTree(['/dashboard']);
    }
    if (paths && paths.length > 0 && !isNavPathAllowed(attempted, paths)) {
      return router.createUrlTree(['/dashboard']);
    }
    return true;
  }

  if (attempted.startsWith('/dashboard/users')) {
    if (paths && paths.length > 0) {
      return isNavPathAllowed('/dashboard/users', paths)
        ? true
        : router.createUrlTree(['/dashboard']);
    }
    return roles.includes('ROLE_ADMIN') ? true : router.createUrlTree(['/dashboard']);
  }

  if (attempted.startsWith('/dashboard/roles')) {
    if (paths && paths.length > 0) {
      return isNavPathAllowed('/dashboard/roles', paths)
        ? true
        : router.createUrlTree(['/dashboard']);
    }
    return roles.includes('ROLE_ADMIN') ? true : router.createUrlTree(['/dashboard']);
  }

  if (!roles.includes('ROLE_ADMIN')) {
    return router.createUrlTree(['/dashboard']);
  }
  if (paths && paths.length > 0 && attempted.startsWith('/dashboard') && !isNavPathAllowed(attempted, paths)) {
    return router.createUrlTree(['/dashboard']);
  }
  return true;
};

function navigationTargetPath(router: Router): string {
  const n = router.getCurrentNavigation();
  if (n?.finalUrl != null) {
    const s = router.serializeUrl(n.finalUrl);
    return stripQuery(s);
  }
  return stripQuery(router.url ?? '');
}

function stripQuery(url: string): string {
  const q = url.split('?')[0] ?? url;
  return q.split('#')[0] ?? q;
}
