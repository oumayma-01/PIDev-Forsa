export type ForsaRoleName = 'CLIENT' | 'AGENT' | 'ADMIN';

export interface ManagedUserRole {
  id: number;
  name: ForsaRoleName;
}

export interface ManagedUser {
  id: number;
  username: string;
  email: string;
  isActive: boolean | null;
  role: ManagedUserRole;
  createdAt?: string | null;
}
