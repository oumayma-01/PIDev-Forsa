package org.example.forsapidev.Services.Implementation;



import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Services.Interfaces.IRoleService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class RoleService implements IRoleService {

    @Autowired

    RoleRepository roleRepository;


    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role findbyId(Integer id) {
        return roleRepository.findById(id).get();
    }
    @Override
    public Role findbyName(ERole name) {
        return roleRepository.findByName(name).get();
    }


    @Override
    public ResponseEntity<?> delete(Integer id) {
        Optional<Role> role = roleRepository.findById(id);
        Role r = role.get();
        roleRepository.delete(r);
        return ResponseEntity.ok("The role has been successfully deleted") ;
    }
}
