package io.pace.backend.domain.model.response;

import io.pace.backend.domain.model.entity.University;
import lombok.*;

@Data
@Builder
public class CustomizationResponse {
    private Long customizationId;
    private String logoUrl;
    private String themeName;
    private String aboutText;
    private StudentResponse studentResponse;
    private AdminResponse adminResponse;
    private UniversityResponse universityResponse;
}
