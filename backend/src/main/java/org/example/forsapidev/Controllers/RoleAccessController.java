package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Services.Interfaces.IRoleAccessService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.payload.request.RoleAccessUpdateRequest;
import org.example.forsapidev.payload.response.RoleAccessCatalogEntryDTO;
import org.example.forsapidev.payload.response.RoleAccessGrantDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/role/access")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class RoleAccessController {

  private final IRoleAccessService roleAccessService;

  @GetMapping("/catalog")
  @PreAuthorize("hasRole('ADMIN')")
  public List<RoleAccessCatalogEntryDTO> catalog() {
    return roleAccessService.catalog();
  }

  @GetMapping("/role/{role}")
  @PreAuthorize("hasRole('ADMIN')")
  public List<RoleAccessGrantDTO> listForRole(@PathVariable("role") ERole role) {
    return roleAccessService.listForRole(role);
  }

  @PutMapping("/role/{role}")
  @PreAuthorize("hasRole('ADMIN')")
  public List<RoleAccessGrantDTO> updateRoleAccess(
      @PathVariable("role") ERole role, @Valid @RequestBody RoleAccessUpdateRequest request) {
    return roleAccessService.updateRoleAccess(role, request);
  }
}
