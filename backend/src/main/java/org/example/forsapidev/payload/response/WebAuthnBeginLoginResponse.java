package org.example.forsapidev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class WebAuthnBeginLoginResponse {
    private String challenge;
    private String rpId;
    private List<String> allowCredentialIds;
    private Long timeout;
    private String userVerification;
}
