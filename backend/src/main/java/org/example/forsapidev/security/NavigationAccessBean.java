package org.example.forsapidev.security;

import org.example.forsapidev.Services.Interfaces.IRoleAccessService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * SpEL bean {@code navigationAccess} : autorise les endpoints "User management" / "Role management"
 * lorsque la matrice {@link IRoleAccessService#permittedNavPathsForRole} inclut le chemin Angular correspondant.
 */
@Component("navigationAccess")
public class NavigationAccessBean {

  private final IRoleAccessService roleAccessService;

  public NavigationAccessBean(IRoleAccessService roleAccessService) {
    this.roleAccessService = roleAccessService;
  }

  public boolean canAccessUsers(Authentication authentication) {
    return permitsPath(authentication, "/dashboard/users");
  }

  public boolean canAccessRoles(Authentication authentication) {
    return permitsPath(authentication, "/dashboard/roles");
  }

  private boolean permitsPath(Authentication authentication, String frontendPath) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return false;
    }
    if (!(authentication.getPrincipal() instanceof UserDetailsImpl principal)) {
      return false;
    }
    ERole role = principal.getAppRole();
    return roleAccessService.permittedNavPathsForRole(role).contains(frontendPath);
  }
}
