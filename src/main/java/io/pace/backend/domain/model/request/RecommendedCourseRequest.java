package io.pace.backend.domain.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedCourseRequest {
    private String courseDescription;
    private double assessmentResult;
    private String resultDescription;
    private List<CareerRequest> careers;
}
