package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.example.forsapidev.entities.UserManagement.ERole;

@Getter
@Setter
public class RoleRequest {


	@NotBlank
	private ERole name;
	private long iduser;

	public ERole getName() {
		return name;
	}

	public void setName(ERole name) {
		this.name = name;
	}

	public long getIduser() {
		return iduser;
	}

	public void setIduser(long iduser) {
		this.iduser = iduser;
	}
}
