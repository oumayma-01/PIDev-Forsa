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

/** Matches backend `GET /api/dashboard/users/overview` (`UserDashboardOverviewDTO`). */
export interface UserDashboardOverview {
  totalUsers: number;
  activeUsers: number;
  inactiveUsers: number;
  newUsersLast30Days: number;
  activationRate: number;
  totalClients: number;
  totalAgents: number;
  totalAdmins: number;
}
