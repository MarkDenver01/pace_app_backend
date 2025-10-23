package io.pace.backend.domain.model.request;

import lombok.Data;

@Data
public class ValidateAccountRequest {
    private String email;
    private int verificationCode;
}
