package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.Repositories.RoleRepository;
import org.example.forsapidev.Repositories.UserRepository;
import org.example.forsapidev.Services.Interfaces.IUserService;
import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.SignupRequest;
import org.example.forsapidev.payload.response.MessageResponse;
import org.example.forsapidev.payload.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
class UserService implements IUserService {
    @Autowired

    UserRepository userRepository;

    @Autowired

    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder encoder;


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
    public ResponseEntity<?> UpdateUser(SignupRequest signUpRequest, long id) {
        User user= userRepository.findById(id).get();
        if (!Objects.equals(user.getUsername(), signUpRequest.getUsername())){
            user.setUsername(signUpRequest.getUsername());
        }
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));

user.setEmail(signUpRequest.getEmail());
        user.setRole(roleRepository.findById(signUpRequest.getIdrole()).get());
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User Updated Succesfully"));
    }



    @Override
    public void delete(Long id) {
        User user = userRepository.findById(id).get();

        userRepository.delete(user);
    }
}
