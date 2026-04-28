package org.example.forsapidev.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WebAuthnBeginLoginRequest {
    private String username;
}
