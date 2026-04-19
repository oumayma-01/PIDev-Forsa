package org.example.forsapidev.Services.Interfaces;

import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.payload.request.RoleAccessUpdateRequest;
import org.example.forsapidev.payload.response.RoleAccessCatalogEntryDTO;
import org.example.forsapidev.payload.response.RoleAccessGrantDTO;

import java.util.List;

public interface IRoleAccessService {

  List<RoleAccessCatalogEntryDTO> catalog();

  List<RoleAccessGrantDTO> listForRole(ERole role);

  List<RoleAccessGrantDTO> updateRoleAccess(ERole role, RoleAccessUpdateRequest request);

  boolean isServletPathPermitted(ERole role, String servletPath);
}
