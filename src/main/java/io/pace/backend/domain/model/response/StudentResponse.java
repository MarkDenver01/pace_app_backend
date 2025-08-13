package io.pace.backend.domain.model.response;

import io.pace.backend.domain.enums.AccountStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long studentId;
    private String userName;
    private String email;
    private LocalDateTime requestedDate;
    private AccountStatus userAccountStatus;
    private Long universityId;
    private String universityName;
}
