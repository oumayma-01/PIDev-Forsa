package org.example.forsapidev.Services.Interfaces;



import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.Role;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IRoleService {

    List<Role> findAll();
    Role findbyId(Integer id);
    Role findbyName(ERole name);
    ResponseEntity<?> delete (Integer id );
}
