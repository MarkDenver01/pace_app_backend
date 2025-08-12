package io.pace.backend.domain.model.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private Long courseId;
    private String courseName;
    private String courseDescription;
    private String status;
    private Long universityId;
    private String universityName;
    private int assessed;
    private int max;



    public CourseResponse(Long courseId, String courseName, String courseDescription, String status, String universityName, Long universityId) {
        this(courseId, courseName, courseDescription, status, universityId, universityName, 0, 0);
    }
}
