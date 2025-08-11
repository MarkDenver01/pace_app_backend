package io.pace.backend.domain.model.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private String courseName;
    private String courseDescription;
    private String status;
    private Long universityId;
    private int assessed;
    private int max;

    public CourseResponse(String courseName, String courseDescription, String status, Long universityId) {
        this(courseName, courseDescription, status, universityId, 0, 0);
    }
}
