package io.pace.backend.domain.model.request;

import lombok.Data;

@Data
public class UpdateUniversityInfoRequest {
    private Long universityId;

    private String universityName;
    private String domainEmail;

    // optional password fields
    private String newPassword;
    private String confirmPassword;
}
