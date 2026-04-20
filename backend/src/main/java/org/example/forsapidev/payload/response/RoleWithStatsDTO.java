package org.example.forsapidev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleWithStatsDTO {
  private Integer id;
  /** Enum name, e.g. {@code ADMIN}. */
  private String name;
  /** Short display title. */
  private String label;
  /** Longer explanation for admins. */
  private String description;
  private long userCount;
}
