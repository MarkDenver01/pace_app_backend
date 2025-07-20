package io.pace.backend.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseResponse {
    private String courseName;
    private String courseDescription;
    private String status;

    public CourseResponse(String courseName, String courseDescription, String status) {
        this.courseName = courseName;
        this.courseDescription = courseDescription;
        this.status = status;
    }
}
