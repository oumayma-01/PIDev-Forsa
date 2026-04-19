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
}

export interface CurrentUser {
  id: number;
  username: string;
  email: string;
  roles: string[];
  hasProfileImage?: boolean;
  oauthAccount?: boolean;
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
