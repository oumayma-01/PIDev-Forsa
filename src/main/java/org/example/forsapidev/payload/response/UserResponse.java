package org.example.forsapidev.payload.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.forsapidev.entities.UserManagement.Role;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

	private Long id;
	private String username;
	private String firstname;
	private String lastname;
	private String email;
	private String password;
	private Role role;
}