package org.example.forsapidev.Services.Implementation;



import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IRoleService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.Role;
import org.example.forsapidev.payload.response.RoleWithStatsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
class RoleService implements IRoleService {

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

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

    @Override
    public List<RoleWithStatsDTO> listRolesWithUserCounts() {
        List<Role> roles = roleRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        List<RoleWithStatsDTO> out = new ArrayList<>();
        for (Role role : roles) {
            ERole name = role.getName();
            long count = userRepository.countByRole_Name(name);
            out.add(new RoleWithStatsDTO(
                    role.getId(),
                    name.name(),
                    humanLabel(name),
                    humanDescription(name),
                    count
            ));
        }
        return out;
    }

    private static String humanLabel(ERole role) {
        return switch (role) {
            case ADMIN -> "Administrator";
            case AGENT -> "Agent";
            case CLIENT -> "Client";
        };
    }

    private static String humanDescription(ERole role) {
        return switch (role) {
            case ADMIN -> "Full access to configuration, users, roles overview, and all modules.";
            case AGENT -> "Handles credit requests, policies, scoring, and client-facing operations.";
            case CLIENT -> "End user: wallet, insurance applications, complaints, and personal dashboard.";
        };
    }
}
