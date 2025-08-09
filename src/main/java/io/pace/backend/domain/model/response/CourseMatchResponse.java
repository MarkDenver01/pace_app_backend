package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseMatchResponse {
    private Long courseId;
    private String courseName;
    private String courseDescription;
    private double matchPercentage;
    private String recommendationMessage;
}
