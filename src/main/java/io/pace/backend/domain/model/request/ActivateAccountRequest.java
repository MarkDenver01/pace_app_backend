package io.pace.backend.domain.model.request;

import lombok.Data;

@Data
public class ActivateAccountRequest {
    private String email;
    private Long universityId;
}
