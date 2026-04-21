export interface RoleAccessCatalogEntry {
  code: string;
  pathPattern: string;
  title: string;
  description: string;
}

export interface RoleAccessGrant {
  resourceCode: string;
  permitted: boolean;
}

export interface RoleAccessUpdateRequest {
  grants: RoleAccessGrant[];
}
