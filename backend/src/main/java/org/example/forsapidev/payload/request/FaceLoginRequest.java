package org.example.forsapidev.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FaceLoginRequest {
    @NotBlank
    private String username;

    @NotEmpty
    private List<Double> descriptor;
}
