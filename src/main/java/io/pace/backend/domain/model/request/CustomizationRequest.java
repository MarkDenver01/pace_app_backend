package io.pace.backend.domain.model.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CustomizationRequest {
    private Long customizationId;
    private String themeName;
    private String aboutText;
    private MultipartFile logoFile;
    private Long universityId;
    private Long studentId;
    private Long adminId;
}