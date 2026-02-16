package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateUserDTO {
	@NotBlank
  private String username;

	@NotBlank
	private String dbname;



}
