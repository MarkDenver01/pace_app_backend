package io.pace.backend.domain.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String jwtToken;
    private String username;
    private String role;
    private AdminResponse adminResponse;
    private StudentResponse studentResponse;

    // admin response
    public LoginResponse( AdminResponse adminResponse, String role, String username, String jwtToken) {
        this.adminResponse = adminResponse;
        this.role = role;
        this.username = username;
        this.jwtToken = jwtToken;
    }

    // student response
    public LoginResponse(String jwtToken, String username, String role, StudentResponse studentResponse) {
        this.jwtToken = jwtToken;
        this.username = username;
        this.role = role;
        this.studentResponse = studentResponse;
    }

    // super admin
    public LoginResponse(String jwtToken, String username, String role) {
        this.jwtToken = jwtToken;
        this.username = username;
        this.role = role;
    }
}
