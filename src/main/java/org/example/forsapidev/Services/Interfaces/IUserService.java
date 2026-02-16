package org.example.forsapidev.Services.Interfaces;


import org.example.forsapidev.entities.UserManagement.User;
import org.example.forsapidev.payload.request.SignupRequest;
import org.example.forsapidev.payload.response.UserResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IUserService {
//    List<User> findallByDbname(String dbname);

    List<User> findall();

    UserResponse findbyId(Long id);

    ResponseEntity<?> updatePassword(Long id, String password);

    public ResponseEntity<?> UpdateUser(SignupRequest signUpRequest, long id);
    void delete (Long id );
}
