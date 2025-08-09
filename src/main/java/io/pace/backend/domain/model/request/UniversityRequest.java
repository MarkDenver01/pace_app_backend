package io.pace.backend.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityRequest {
    @NotBlank(message = "University name is required")
    private String universityName;
}
