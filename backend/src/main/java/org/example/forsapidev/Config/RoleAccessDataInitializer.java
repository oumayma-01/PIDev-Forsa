package org.example.forsapidev.config;

import lombok.RequiredArgsConstructor;
import org.example.forsapidev.Repositories.RoleAccessSettingRepository;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.RoleAccessSetting;
import org.example.forsapidev.security.access.PlatformAccessResource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Initialise la table des droits d'accès au premier démarrage (valeurs par défaut alignées sur le catalogue).
 */
@Component
@RequiredArgsConstructor
public class RoleAccessDataInitializer implements ApplicationRunner {

  private final RoleAccessSettingRepository roleAccessSettingRepository;

  @Override
  public void run(ApplicationArguments args) {
    if (roleAccessSettingRepository.count() > 0) {
      return;
    }
    for (ERole role : ERole.values()) {
      for (PlatformAccessResource res : PlatformAccessResource.values()) {
        roleAccessSettingRepository.save(new RoleAccessSetting(role, res, res.defaultFor(role)));
      }
    }
  }
}
