package io.pace.backend.domain.model.request;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CustomizationRequest {
    private Long id;
    private String themeName;
    private String aboutText;
    private MultipartFile logoFile;
}