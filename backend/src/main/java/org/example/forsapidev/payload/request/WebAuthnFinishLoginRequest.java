package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebAuthnFinishLoginRequest {
    private String username;

    @NotBlank
    private String credentialId;

    @NotBlank
    private String clientDataJSON;

    private String authenticatorData;
    private String signature;
}
