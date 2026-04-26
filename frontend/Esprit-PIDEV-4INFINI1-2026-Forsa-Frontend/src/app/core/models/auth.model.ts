export interface JwtResponse {
  token: string;
  type?: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
  hasProfileImage?: boolean;
  /** Google-created account: user has no known password until they set one in profile. */
  oauthAccount?: boolean;
  /** Allowed Angular routes for the sidebar, derived from the user's role. */
  allowedNavPaths?: string[];
}

export interface CurrentUser {
  id: number;
  username: string;
  email: string;
  roles: string[];
  hasProfileImage?: boolean;
  oauthAccount?: boolean;
  /** Allowed Angular routes for the sidebar, derived from the user's role. */
  allowedNavPaths?: string[];
}

export interface MessageResponse {
  message: string;
}

export interface SignupPayload {
  username: string;
  email: string;
  password: string;
  idrole: number;
}

export interface WebAuthnBeginRegisterResponse {
  challenge: string;
  rpId: string;
  rpName: string;
  userId: string;
  userName: string;
  userDisplayName: string;
  excludeCredentialIds: string[];
  timeout: number;
  authenticatorAttachment: 'platform' | 'cross-platform';
  residentKey: 'required' | 'preferred' | 'discouraged';
  userVerification: 'required' | 'preferred' | 'discouraged';
}

export interface WebAuthnBeginLoginResponse {
  challenge: string;
  rpId: string;
  allowCredentialIds: string[];
  timeout: number;
  userVerification: 'required' | 'preferred' | 'discouraged';
}

export interface WebAuthnCredentialItem {
  credentialId: string;
  deviceName: string;
  transports?: string;
  createdAt: string;
  lastUsedAt?: string;
}
