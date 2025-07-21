package io.pace.backend.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseMatchResponse {
    private String courseName;
    private String courseDescription;
    private int matchPercentage;
    private String recommendationMessage;
}
