package org.example.forsapidev.Repositories;

import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.RoleAccessSetting;
import org.example.forsapidev.security.access.PlatformAccessResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleAccessSettingRepository extends JpaRepository<RoleAccessSetting, Long> {

  List<RoleAccessSetting> findAllByRoleNameOrderByResourceAsc(ERole roleName);

  Optional<RoleAccessSetting> findByRoleNameAndResource(ERole roleName, PlatformAccessResource resource);
}
