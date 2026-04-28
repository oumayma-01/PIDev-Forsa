package org.example.forsapidev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class WebAuthnBeginRegisterResponse {
    private String challenge;
    private String rpId;
    private String rpName;
    private String userId;
    private String userName;
    private String userDisplayName;
    private List<String> excludeCredentialIds;
    private Long timeout;
    private String authenticatorAttachment;
    private String residentKey;
    private String userVerification;
}
