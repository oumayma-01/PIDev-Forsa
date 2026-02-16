package org.example.forsapidev.payload.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.entities.UserManagement.Role;

import java.util.Date;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

	private Long id;
	private String username;
	private String email;
	private String role;
	private boolean isActive;
	private Date createdAt;
}