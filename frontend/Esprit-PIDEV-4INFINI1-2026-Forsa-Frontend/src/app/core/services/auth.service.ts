import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, firstValueFrom, tap, timeout } from 'rxjs';
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
            hasProfileImage: res.hasProfileImage ?? false,
            oauthAccount: res.oauthAccount ?? false,
          });
        }),
      );
  }

  signup(payload: SignupPayload): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiBaseUrl}/auth/signup`, payload);
  }

  /** Public URL returned by the backend to start Google OAuth (full-page redirect). */
  getGoogleLoginUrl(): Observable<MessageResponse> {
    return this.http.get<MessageResponse>(`${environment.apiBaseUrl}/auth/google-login-url`);
  }

  /** Stores the JWT from OAuth redirect and loads the current user from the API. */
  completeOAuthSession(token: string): Observable<CurrentUser> {
    if (typeof localStorage !== 'undefined') {
      localStorage.setItem(TOKEN_KEY, token);
    }
    return this.http.get<CurrentUser>(`${environment.apiBaseUrl}/auth/current`).pipe(
      tap((u) => this.currentUser.set(u)),
    );
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
        this.http.get<CurrentUser>(`${environment.apiBaseUrl}/auth/current`).pipe(timeout(10_000)),
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

  /** Reloads the signed-in user from {@code GET /auth/current} (e.g. after profile or avatar changes). */
  refreshCurrentUser(): Observable<CurrentUser> {
    return this.http.get<CurrentUser>(`${environment.apiBaseUrl}/auth/current`).pipe(
      tap((u) => this.currentUser.set(u)),
    );
  }
}
