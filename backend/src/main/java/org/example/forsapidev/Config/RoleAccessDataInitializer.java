package org.example.forsapidev.Config;

import org.example.forsapidev.Repositories.RoleNavAccessSettingRepository;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.RoleNavAccessSetting;
import org.example.forsapidev.security.nav.DashboardNavFeature;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Seeds default sidebar navigation access on first application startup. */
@Component
public class RoleAccessDataInitializer implements ApplicationRunner {

  private final RoleNavAccessSettingRepository roleNavAccessSettingRepository;
  
  public RoleAccessDataInitializer(RoleNavAccessSettingRepository roleNavAccessSettingRepository) {
    this.roleNavAccessSettingRepository = roleNavAccessSettingRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (roleNavAccessSettingRepository.count() > 0) {
      return;
    }
    for (ERole role : ERole.values()) {
      for (DashboardNavFeature feature : DashboardNavFeature.values()) {
        boolean permitted = feature.defaultPermittedFor(role);
        roleNavAccessSettingRepository.save(new RoleNavAccessSetting(role, feature, permitted));
      }
    }
  }
}
