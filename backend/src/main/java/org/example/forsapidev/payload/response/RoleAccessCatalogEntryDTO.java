package org.example.forsapidev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoleAccessCatalogEntryDTO {
  private String code;
  private String pathPattern;
  private String title;
  private String description;
}
