package io.pace.backend.domain.model.request;

import io.pace.backend.domain.enums.AccountStatus;
import lombok.Data;

@Data
public class VerifyAccountRequest {
    private String email;
    private int verificationCode;
}
