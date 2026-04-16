package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.AgentRegistryService;
import org.example.forsapidev.Services.Interfaces.IUserService;
import org.example.forsapidev.entities.UserManagement.ERole;
import org.example.forsapidev.entities.UserManagement.Role;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.SignupRequest;
import org.example.forsapidev.payload.response.MessageResponse;
import org.example.forsapidev.payload.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
class UserService implements IUserService {
    @Autowired

    UserRepository userRepository;

    @Autowired

    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    AgentRegistryService agentRegistryService;


    @Override
    public List<User> findall() {
        return userRepository.findAll();
    }


    @Override
    public UserResponse findbyId(Long id) {
        User val = userRepository.findById(id).get();
        UserResponse u = new UserResponse();
        u.setId(val.getId());
        u.setUsername(val.getUsername());
        u.setActive(val.getIsActive());
        u.setCreatedAt(val.getCreatedAt());
        u.setEmail(val.getEmail());
        u.setRole(val.getRole().getName().name());


        return u;
    }
    @Override
    public ResponseEntity<?> updatePassword(Long id, String password) {
        User val = userRepository.findById(id).get();
        val.setPasswordHash(encoder.encode(password));
        userRepository.save(val);
        return ResponseEntity.ok(new MessageResponse("The password has been successfully updated")) ;
    }
    @Override
    @Transactional
    public ResponseEntity<?> UpdateUser(SignupRequest signUpRequest, long id) {
        User user= userRepository.findById(id).get();
        if (!Objects.equals(user.getUsername(), signUpRequest.getUsername())){
            user.setUsername(signUpRequest.getUsername());
        }
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));

user.setEmail(signUpRequest.getEmail());
        user.setRole(roleRepository.findById(signUpRequest.getIdrole()).get());
        userRepository.save(user);
        agentRegistryService.syncAgentForUser(user.getId());

        return ResponseEntity.ok(new MessageResponse("User Updated Succesfully"));
    }

    @Override
    @Transactional
    public ResponseEntity<?> setUserActive(long id, boolean active) {
        return userRepository.findById(id).map(user -> {
        user.setIsActive(active);
        userRepository.save(user);
        agentRegistryService.syncAgentForUser(id);
        return ResponseEntity.ok(new MessageResponse(active ? "User activated" : "User deactivated"));
        }).orElseGet(() -> ResponseEntity.status(404).body(new MessageResponse("User not found")));
    }

    @Override
    @Transactional
    public ResponseEntity<?> createAgent(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }
        Role role = roleRepository.findById(signUpRequest.getIdrole()).orElse(null);
        if (role == null) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Role is not found."));
        }
        if (role.getName() != ERole.AGENT) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: idrole must reference the AGENT role."));
        }
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        user.setRole(role);
        user.setIsActive(true);
        user.setCreatedAt(new Date());
        userRepository.save(user);
        agentRegistryService.syncAgentForUser(user.getId());
        return ResponseEntity.ok(new MessageResponse("Agent user created successfully."));
    }


    @Override
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id).get();
        agentRegistryService.deleteAgentForUser(id);
        userRepository.delete(user);
    }
}
