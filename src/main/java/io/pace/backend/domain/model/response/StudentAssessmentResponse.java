package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StudentAssessmentResponse {
    private Long studentId;
    private String userName;
    private String email;
    private String enrollmentStatus;
    private String enrolledUniversity;
    private Long universityId;
    private String createdDateTime;
    private String assessmentStatus;
    private List<RecommendedCourseResponse> recommendedCourses;
}
