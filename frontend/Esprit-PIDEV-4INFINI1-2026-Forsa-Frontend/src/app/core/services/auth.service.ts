import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, firstValueFrom, tap, timeout } from 'rxjs';
import { environment } from '../../../environments/environment';
import { WebAuthnBrowserService } from './webauthn-browser.service';
import type {
  CurrentUser,
  JwtResponse,
  MessageResponse,
  SignupPayload,
  WebAuthnBeginLoginResponse,
  WebAuthnBeginRegisterResponse,
  WebAuthnCredentialItem,
} from '../models/auth.model';

const TOKEN_KEY = 'forsa_access_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly webauthn = inject(WebAuthnBrowserService);

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
        tap((res) => this.applyJwtSession(res)),
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

  supportsBiometrics(): boolean {
    return this.webauthn.isSupported();
  }

  async loginWithPasskey(username: string): Promise<void> {
    if (!this.webauthn.isSupported()) {
      throw new Error('Biometric login is not supported on this browser.');
    }
    const begin = await firstValueFrom(
      this.http.post<WebAuthnBeginLoginResponse>(`${environment.apiBaseUrl}/auth/webauthn/login/begin`, { username }),
    );
    let assertion: PublicKeyCredential;
    try {
      assertion = await this.webauthn.getCredential({
        challenge: this.webauthn.toBuffer(begin.challenge),
        rpId: begin.rpId,
        allowCredentials: (begin.allowCredentialIds ?? []).map((id) => ({
          id: this.webauthn.toBuffer(id),
          type: 'public-key',
        })),
        timeout: begin.timeout ?? 60000,
        userVerification: begin.userVerification ?? 'preferred',
      });
    } catch (e: unknown) {
      const host = typeof window !== 'undefined' ? window.location.hostname : '';
      const dom = e instanceof DOMException ? e : null;
      if (dom?.name === 'SecurityError' || (e instanceof Error && /rpId|Relying Party/i.test(e.message))) {
        throw new Error(
          `Passkey login blocked: page host "${host}" vs server rpId "${begin.rpId}".`,
        );
      }
      if (dom?.name === 'NotAllowedError') {
        throw new Error('Passkey sign-in was cancelled or no credential matched.');
      }
      throw e instanceof Error ? e : new Error(String(e));
    }

    const response = assertion.response as AuthenticatorAssertionResponse;
    const credentialId = this.webauthn.fromBuffer(assertion.rawId);
    const jwt = await firstValueFrom(
      this.http.post<JwtResponse>(`${environment.apiBaseUrl}/auth/webauthn/login/finish`, {
        username,
        credentialId,
        clientDataJSON: this.webauthn.fromBuffer(response.clientDataJSON),
        authenticatorData: this.webauthn.fromBuffer(response.authenticatorData),
        signature: this.webauthn.fromBuffer(response.signature),
      }),
    );
    this.applyJwtSession(jwt);
  }

  async loginWithFace(username: string, descriptor: number[]): Promise<void> {
    const jwt = await firstValueFrom(
      this.http.post<JwtResponse>(`${environment.apiBaseUrl}/auth/face/login`, {
        username,
        descriptor,
      }),
    );
    this.applyJwtSession(jwt);
  }

  enrollFace(descriptor: number[]): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiBaseUrl}/auth/face/enroll`, { descriptor });
  }

  async registerCurrentDevicePasskey(deviceName?: string): Promise<void> {
    if (!this.webauthn.isSupported()) {
      throw new Error('Passkeys are not supported on this browser.');
    }
    const begin = await firstValueFrom(
      this.http.post<WebAuthnBeginRegisterResponse>(`${environment.apiBaseUrl}/auth/webauthn/register/begin`, {
        deviceName: deviceName ?? 'Current device',
      }),
    );
    let credential: PublicKeyCredential;
    try {
      credential = await this.webauthn.createCredential({
        challenge: this.webauthn.toBuffer(begin.challenge),
        rp: { id: begin.rpId, name: begin.rpName },
        user: {
          id: this.webauthn.toBuffer(begin.userId),
          name: begin.userName,
          displayName: begin.userDisplayName,
        },
        pubKeyCredParams: [
          { type: 'public-key', alg: -7 },
          { type: 'public-key', alg: -257 },
        ],
        timeout: begin.timeout ?? 60000,
        excludeCredentials: (begin.excludeCredentialIds ?? []).map((id) => ({
          type: 'public-key',
          id: this.webauthn.toBuffer(id),
        })),
        authenticatorSelection: {
          authenticatorAttachment: begin.authenticatorAttachment ?? 'platform',
          residentKey: begin.residentKey ?? 'preferred',
          userVerification: begin.userVerification ?? 'preferred',
        },
        attestation: 'none',
      });
    } catch (e: unknown) {
      const host = typeof window !== 'undefined' ? window.location.hostname : '';
      const dom = e instanceof DOMException ? e : null;
      if (dom?.name === 'SecurityError' || (e instanceof Error && /rpId|Relying Party/i.test(e.message))) {
        throw new Error(
          `Passkey blocked: this page is "${host}" but the server sent rpId "${begin.rpId}". They must match (or use a valid parent domain).`,
        );
      }
      if (dom?.name === 'NotAllowedError') {
        throw new Error('Passkey registration was cancelled or not allowed on this device.');
      }
      throw e instanceof Error ? e : new Error(String(e));
    }
    const response = credential.response as AuthenticatorAttestationResponse;
    await firstValueFrom(
      this.http.post<MessageResponse>(`${environment.apiBaseUrl}/auth/webauthn/register/finish`, {
        credentialId: this.webauthn.fromBuffer(credential.rawId),
        publicKey: this.webauthn.fromBuffer(response.getPublicKey() ?? new ArrayBuffer(0)),
        clientDataJSON: this.webauthn.fromBuffer(response.clientDataJSON),
        attestationObject: this.webauthn.fromBuffer(response.attestationObject),
        transports: this.webauthn.transportsToCsv(response.getTransports?.()),
        deviceName: deviceName ?? 'Current device',
      }),
    );
  }

  listPasskeys(): Observable<WebAuthnCredentialItem[]> {
    return this.http.get<WebAuthnCredentialItem[]>(`${environment.apiBaseUrl}/auth/webauthn/credentials`);
  }

  removePasskey(credentialId: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${environment.apiBaseUrl}/auth/webauthn/credentials/${encodeURIComponent(credentialId)}`);
  }

  private applyJwtSession(res: JwtResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    this.currentUser.set({
      id: res.id,
      username: res.username,
      email: res.email,
      roles: res.roles ?? [],
      hasProfileImage: res.hasProfileImage ?? false,
      oauthAccount: res.oauthAccount ?? false,
      allowedNavPaths: res.allowedNavPaths,
    });
  }
}
