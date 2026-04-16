export const environment = {
  production: false,
  /** Spring context path + `/api` (no trailing slash) */
  apiBaseUrl: 'http://localhost:8089/forsaPidev/api',
  /** Default DB role id for `CLIENT` after DefaultUserConfig seed (ADMIN=1, CLIENT=2, AGENT=3). */
  defaultClientRoleId: 2,
  /** Role id for `AGENT` (same seed convention). */
  defaultAgentRoleId: 3,
};
