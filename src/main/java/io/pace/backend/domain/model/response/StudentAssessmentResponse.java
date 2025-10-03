package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentAssessmentResponse {
    private String email;
    private String userName;
    private String course;
    private String status;
    private String lastActive;
}
