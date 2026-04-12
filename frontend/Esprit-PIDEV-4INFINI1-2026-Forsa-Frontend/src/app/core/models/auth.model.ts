export interface JwtResponse {
  token: string;
  type?: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
}

export interface CurrentUser {
  id: number;
  username: string;
  email: string;
  roles: string[];
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
