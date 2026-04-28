package org.example.forsapidev.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class WebAuthnCredentialResponse {
    private String credentialId;
    private String deviceName;
    private String transports;
    private Date createdAt;
    private Date lastUsedAt;
}
