package io.pace.backend.domain.model.request;

import io.pace.backend.domain.enums.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Category is required")
    private QuestionCategory category;

    @NotBlank(message = "Question text is required")
    private String question;
}
