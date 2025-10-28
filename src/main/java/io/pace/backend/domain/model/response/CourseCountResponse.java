package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseCountResponse {
    private String courseDescription;
    private int count; // total number of students who got this course
}
