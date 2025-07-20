package io.pace.backend.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank
    private String courseName;
    @NotBlank
    private String courseDescription;
    @NotBlank
    private String status;
}
