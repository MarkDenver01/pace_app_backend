package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class RecommendedCourseResponse {
    private Long courseId;
    private String courseDescription;
    private double assessmentResult;
    private String resultDescription;
    private Long studentId;
    private List<RecommendedCareerResponse> careers;
}
