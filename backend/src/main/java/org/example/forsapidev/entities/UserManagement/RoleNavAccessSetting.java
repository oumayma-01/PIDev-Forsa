package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.security.nav.DashboardNavFeature;

@Entity
@Table(
    name = "role_nav_access_settings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"role_name", "feature_code"}))
@Getter
@Setter
@NoArgsConstructor
public class RoleNavAccessSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_name", nullable = false, length = 20)
  private ERole roleName;

  @Enumerated(EnumType.STRING)
  @Column(name = "feature_code", nullable = false, length = 64)
  private DashboardNavFeature feature;

  @Column(name = "frontend_path", nullable = false, length = 255)
  private String frontendPath;

  @Column(nullable = false)
  private boolean permitted;

  public RoleNavAccessSetting(ERole roleName, DashboardNavFeature feature, boolean permitted) {
    this.roleName = roleName;
    this.feature = feature;
    this.frontendPath = feature.getFrontendPath();
    this.permitted = permitted;
  }
}
