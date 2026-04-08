package org.example.forsapidev.Services.Interfaces;


import org.example.forsapidev.payload.request.ForgottenPasswordRequest;
import org.example.forsapidev.payload.request.LoginRequest;
import org.example.forsapidev.payload.request.ResetRequest;
import org.example.forsapidev.payload.request.SignupRequest;
import org.springframework.http.ResponseEntity;

public interface IAuthService {

    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest);

    ResponseEntity<?> register(SignupRequest signUpRequest);

    ResponseEntity<?> ValidateUser(long iduser);

    ResponseEntity<?> ForgottenPassword(ForgottenPasswordRequest loginRequest) throws Exception;

    ResponseEntity<?> resetpass(ResetRequest restRequest) throws Exception;
}
