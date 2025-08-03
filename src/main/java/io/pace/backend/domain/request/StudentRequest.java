package io.pace.backend.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class StudentRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private Long studentId;
}
