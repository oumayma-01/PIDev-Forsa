package org.example.forsapidev.Services.Implementation;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.RoleAccessSettingRepository;
import org.example.forsapidev.Services.Interfaces.IRoleAccessService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.RoleAccessSetting;
import org.example.forsapidev.payload.request.RoleAccessUpdateRequest;
import org.example.forsapidev.payload.response.RoleAccessCatalogEntryDTO;
import org.example.forsapidev.payload.response.RoleAccessGrantDTO;
import org.example.forsapidev.security.access.PlatformAccessResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleAccessService implements IRoleAccessService {

  private final RoleAccessSettingRepository roleAccessSettingRepository;

  @Override
  public List<RoleAccessCatalogEntryDTO> catalog() {
    List<RoleAccessCatalogEntryDTO> out = new ArrayList<>();
    for (PlatformAccessResource r : PlatformAccessResource.values()) {
      out.add(new RoleAccessCatalogEntryDTO(
          r.name(),
          r.getPathPattern(),
          r.getTitle(),
          r.getDescription()));
    }
    return out;
  }

  @Override
  public List<RoleAccessGrantDTO> listForRole(ERole role) {
    Map<PlatformAccessResource, Boolean> map = loadEffectiveMap(role);
    List<RoleAccessGrantDTO> out = new ArrayList<>();
    for (PlatformAccessResource r : PlatformAccessResource.values()) {
      out.add(new RoleAccessGrantDTO(r.name(), Boolean.TRUE.equals(map.get(r))));
    }
    return out;
  }

  @Override
  @Transactional
  public List<RoleAccessGrantDTO> updateRoleAccess(ERole role, RoleAccessUpdateRequest request) {
    for (RoleAccessGrantDTO grant : request.getGrants()) {
      PlatformAccessResource resource;
      try {
        resource = PlatformAccessResource.valueOf(grant.getResourceCode());
      } catch (IllegalArgumentException ex) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Ressource inconnue: " + grant.getResourceCode());
      }
      RoleAccessSetting row = roleAccessSettingRepository
          .findByRoleNameAndResource(role, resource)
          .orElseGet(() -> new RoleAccessSetting(role, resource, resource.defaultFor(role)));
      row.setPermitted(grant.isPermitted());
      roleAccessSettingRepository.save(row);
    }
    return listForRole(role);
  }

  @Override
  public boolean isServletPathPermitted(ERole role, String servletPath) {
    if (role == ERole.ADMIN) {
      return true;
    }
    Optional<PlatformAccessResource> resource = PlatformAccessResource.resolveForPath(servletPath);
    if (resource.isEmpty()) {
      return true;
    }
    return loadEffectiveMap(role).getOrDefault(resource.get(), resource.get().defaultFor(role));
  }

  private Map<PlatformAccessResource, Boolean> loadEffectiveMap(ERole role) {
    EnumMap<PlatformAccessResource, Boolean> defaults = new EnumMap<>(PlatformAccessResource.class);
    for (PlatformAccessResource r : PlatformAccessResource.values()) {
      defaults.put(r, r.defaultFor(role));
    }
    List<RoleAccessSetting> rows = roleAccessSettingRepository.findAllByRoleNameOrderByResourceAsc(role);
    for (RoleAccessSetting row : rows) {
      defaults.put(row.getResource(), row.isPermitted());
    }
    return defaults;
  }
}
