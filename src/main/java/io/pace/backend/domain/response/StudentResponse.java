package io.pace.backend.domain.response;

import io.pace.backend.domain.enums.AccountStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class StudentResponse {
    private Long studentId;
    private String userName;
    private String email;
    private LocalDateTime requestedDate;
    private AccountStatus userAccountStatus;

    public StudentResponse(Long studentId, String userName, String email, LocalDateTime requestedDate, AccountStatus userAccountStatus) {
        this.studentId = studentId;
        this.userName = userName;
        this.email = email;
        this.requestedDate = requestedDate;
        this.userAccountStatus = userAccountStatus;
    }
}
