package org.example.forsapidev.entities.UserManagement;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.security.access.PlatformAccessResource;

@Entity
@Table(
    name = "role_access_settings",
    uniqueConstraints = @UniqueConstraint(columnNames = {"role_name", "resource_code"}))
@Getter
@Setter
@NoArgsConstructor
public class RoleAccessSetting {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_name", nullable = false, length = 20)
  private ERole roleName;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_code", nullable = false, length = 80)
  private PlatformAccessResource resource;

  @Column(nullable = false)
  private boolean permitted;

  public RoleAccessSetting(ERole roleName, PlatformAccessResource resource, boolean permitted) {
    this.roleName = roleName;
    this.resource = resource;
    this.permitted = permitted;
  }
}
