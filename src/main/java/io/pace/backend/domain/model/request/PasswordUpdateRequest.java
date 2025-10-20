package io.pace.backend.domain.model.request;

import lombok.Data;

@Data
public class PasswordUpdateRequest {
    private String email;
    private Long universityId;
    private String newPassword;
    private String emailDomain;
}
