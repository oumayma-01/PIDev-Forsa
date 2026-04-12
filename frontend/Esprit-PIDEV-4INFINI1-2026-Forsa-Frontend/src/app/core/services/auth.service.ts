import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, firstValueFrom, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { CurrentUser, JwtResponse, MessageResponse, SignupPayload } from '../models/auth.model';

const TOKEN_KEY = 'forsa_access_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  readonly currentUser = signal<CurrentUser | null>(null);

  getAccessToken(): string | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getAccessToken();
  }

  login(username: string, password: string): Observable<JwtResponse> {
    return this.http
      .post<JwtResponse>(`${environment.apiBaseUrl}/auth/signin`, { username, password })
      .pipe(
        tap((res) => {
          localStorage.setItem(TOKEN_KEY, res.token);
          this.currentUser.set({
            id: res.id,
            username: res.username,
            email: res.email,
            roles: res.roles ?? [],
          });
        }),
      );
  }

  signup(payload: SignupPayload): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiBaseUrl}/auth/signup`, payload);
  }

  /** Sends a reset link to the given email (backend `POST /auth/ForgottenPassword`). */
  requestPasswordReset(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiBaseUrl}/auth/ForgottenPassword`, { email });
  }

  /** Validates JWT with backend and refreshes {@link currentUser}. */
  async ensureSessionFromApi(): Promise<boolean> {
    const token = this.getAccessToken();
    if (!token) {
      this.currentUser.set(null);
      return false;
    }
    if (this.currentUser()) {
      return true;
    }
    try {
      const u = await firstValueFrom(
        this.http.get<CurrentUser>(`${environment.apiBaseUrl}/auth/current`),
      );
      this.currentUser.set(u);
      return true;
    } catch {
      this.clearSession();
      return false;
    }
  }

  clearSession(): void {
    localStorage.removeItem(TOKEN_KEY);
    this.currentUser.set(null);
  }

  logout(): void {
    this.clearSession();
    void this.router.navigateByUrl('/login');
  }
}
