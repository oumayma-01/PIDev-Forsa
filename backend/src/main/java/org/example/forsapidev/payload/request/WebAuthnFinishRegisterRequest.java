package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebAuthnFinishRegisterRequest {
    @NotBlank
    private String credentialId;

    private String publicKey;

    @NotBlank
    private String clientDataJSON;

    private String attestationObject;
    private String transports;
    private String deviceName;
}
