package io.pace.backend.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Course name is required")
    private String courseName;
    @NotBlank(message = "Course description is required")
    private String courseDescription;

    private String status;
    private Long universityId;
}
