package io.pace.backend.domain.model.request;

import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String token;       // encrypted token from URL (front-end reads query param & posts body)
    private String newPassword;
}
