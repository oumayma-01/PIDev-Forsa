package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.example.forsapidev.payload.response.RoleAccessGrantDTO;

import java.util.List;

@Getter
@Setter
public class RoleAccessUpdateRequest {

  @NotEmpty
  private List<RoleAccessGrantDTO> grants;
}
