package org.example.forsapidev.Services.Implementation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.RoleNavAccessSettingRepository;
import org.example.forsapidev.Services.Interfaces.IRoleAccessService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.RoleNavAccessSetting;
import org.example.forsapidev.payload.request.RoleAccessUpdateRequest;
import org.example.forsapidev.payload.response.RoleAccessCatalogEntryDTO;
import org.example.forsapidev.payload.response.RoleAccessGrantDTO;
import org.example.forsapidev.security.nav.DashboardNavFeature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class RoleAccessService implements IRoleAccessService {

  private final RoleNavAccessSettingRepository roleNavAccessSettingRepository;

  @Override
  public List<RoleAccessCatalogEntryDTO> catalog() {
    List<RoleAccessCatalogEntryDTO> out = new ArrayList<>();
    for (DashboardNavFeature f : DashboardNavFeature.values()) {
      out.add(new RoleAccessCatalogEntryDTO(
          f.getCode(),
          f.getFrontendPath(),
          f.getTitle(),
          f.getDescription()));
    }
    return out;
  }

  @Override
  public List<RoleAccessGrantDTO> listForRole(ERole role) {
    Map<DashboardNavFeature, Boolean> map = loadEffectiveMap(role);
    List<RoleAccessGrantDTO> out = new ArrayList<>();
    for (DashboardNavFeature f : DashboardNavFeature.values()) {
      out.add(new RoleAccessGrantDTO(f.getCode(), Boolean.TRUE.equals(map.get(f))));
    }
    return out;
  }

  @Override
  @Transactional
  public List<RoleAccessGrantDTO> updateRoleAccess(ERole role, RoleAccessUpdateRequest request) {
    for (RoleAccessGrantDTO grant : request.getGrants()) {
      DashboardNavFeature feature;
      try {
        feature = DashboardNavFeature.fromCode(grant.getResourceCode());
      } catch (IllegalArgumentException ex) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST, "Unknown feature: " + grant.getResourceCode());
      }
      RoleNavAccessSetting row =
          roleNavAccessSettingRepository
              .findByRoleNameAndFeature(role, feature)
              .orElseGet(() -> new RoleNavAccessSetting(role, feature, feature.defaultPermittedFor(role)));
      row.setPermitted(grant.isPermitted());
      row.setFrontendPath(feature.getFrontendPath());
      roleNavAccessSettingRepository.save(row);
    }
    return listForRole(role);
  }

  @Override
  public List<String> permittedNavPathsForRole(ERole role) {
    Map<DashboardNavFeature, Boolean> map = loadEffectiveMap(role);
    List<String> out = new ArrayList<>();
    for (DashboardNavFeature f : DashboardNavFeature.values()) {
      if (Boolean.TRUE.equals(map.get(f))) {
        out.add(f.getFrontendPath());
      }
    }
    return out;
  }

  private Map<DashboardNavFeature, Boolean> loadEffectiveMap(ERole role) {
    EnumMap<DashboardNavFeature, Boolean> defaults = new EnumMap<>(DashboardNavFeature.class);
    for (DashboardNavFeature f : DashboardNavFeature.values()) {
      defaults.put(f, f.defaultPermittedFor(role));
    }
    List<RoleNavAccessSetting> rows = roleNavAccessSettingRepository.findAllByRoleNameOrderByFeatureAsc(role);
    for (RoleNavAccessSetting row : rows) {
      defaults.put(row.getFeature(), row.isPermitted());
    }
    return defaults;
  }
}
