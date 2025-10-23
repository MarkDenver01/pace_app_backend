package io.pace.backend.domain.model.request;

import lombok.Data;

@Data
public class VerificationCodeRequest {
    private String email;
}
