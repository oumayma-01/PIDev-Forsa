package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.Services.Interfaces.IRoleService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.Role;
import org.example.forsapidev.entities.UserManagement.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/role")
public class RoleController {
  @Autowired
  IRoleService iRoleService;

  @SecurityRequirement(name = "Bearer Authentication")
  @GetMapping("/all")
  public List<Role> all() {
    return iRoleService.findAll();
  }
  @GetMapping("/find/{id}")
  public Role find(@PathVariable("id") int id)
  {
    Role u=iRoleService.findbyId(id);



    return u;
  }
  @GetMapping("/findn/{name}")
  public Role findN(@PathVariable("name") ERole name)
  {
    Role u=iRoleService.findbyName(name);



    return u;
  }
  @DeleteMapping("/delete/{id}")
  public String delete(@PathVariable("id") int id ) {
    iRoleService.delete(id);
    return "Deleted successfully";
  }
}
