package io.pace.backend.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsernameResponse {
    private String username;

    public UsernameResponse(String username) {
        this.username = username;
    }
}
