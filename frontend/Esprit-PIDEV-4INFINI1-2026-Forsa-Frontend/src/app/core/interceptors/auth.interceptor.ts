import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

const TOKEN_KEY = 'forsa_access_token';

export function readStoredAccessToken(): string | null {
  if (typeof localStorage === 'undefined') {
    return null;
  }
  return localStorage.getItem(TOKEN_KEY);
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = readStoredAccessToken();
  const isApi = req.url.startsWith(environment.apiBaseUrl);
  if (token && isApi) {
    return next(
      req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      }),
    );
  }
  return next(req);
};
