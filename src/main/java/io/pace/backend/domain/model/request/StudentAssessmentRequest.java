package io.pace.backend.domain.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssessmentRequest {
    private String email;
    private String userName;
    private Long courseId;
    private Long universityId;
}
