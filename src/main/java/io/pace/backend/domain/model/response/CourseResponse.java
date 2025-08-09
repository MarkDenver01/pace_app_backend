package io.pace.backend.domain.model.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private String courseName;
    private String courseDescription;
    private String status;
}
