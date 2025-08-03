package io.pace.backend.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudentResponse {
    private String userName;
    private String email;
    private String requestedDate;
    private int userAccountStatus;

    public StudentResponse(String userName, String email, String requestedDate, int userAccountStatus) {
        this.userName = userName;
        this.email = email;
        this.requestedDate = requestedDate;
        this.userAccountStatus = userAccountStatus;
    }
}
