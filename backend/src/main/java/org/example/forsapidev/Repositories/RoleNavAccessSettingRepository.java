package org.example.forsapidev.Repositories;

import java.util.List;
import java.util.Optional;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.RoleNavAccessSetting;
import org.example.forsapidev.security.nav.DashboardNavFeature;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleNavAccessSettingRepository extends JpaRepository<RoleNavAccessSetting, Long> {

  List<RoleNavAccessSetting> findAllByRoleNameOrderByFeatureAsc(ERole roleName);

  Optional<RoleNavAccessSetting> findByRoleNameAndFeature(ERole roleName, DashboardNavFeature feature);
}
