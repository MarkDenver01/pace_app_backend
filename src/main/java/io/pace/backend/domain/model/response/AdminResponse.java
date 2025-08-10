package io.pace.backend.domain.model.response;

import io.pace.backend.domain.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private Long adminId;
    private String userName;
    private String email;
    private LocalDateTime createdAt;
    private AccountStatus accountStatus;
    private Long universityId;
}
