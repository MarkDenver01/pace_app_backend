package io.pace.backend.domain.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssessmentRequest {
    private String email;
    private String userName;
    private String enrollmentStatus;
    private String enrolledUniversity;
    private String assessmentStatus;
    private Long universityId;
    private List<RecommendedCourseRequest> recommendedCourseRequests;
}
