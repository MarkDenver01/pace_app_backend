package io.pace.backend.domain.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LoginResponse {
    private String jwtToken;
    private String username;
    private String role;

    public LoginResponse(String username, String role, String jwtToken) {
        this.username = username;
        this.role = role;
        this.jwtToken = jwtToken;
    }
}
