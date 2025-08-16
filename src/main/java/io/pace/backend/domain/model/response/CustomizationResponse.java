package io.pace.backend.domain.model.response;

import lombok.*;

@Data
@Builder
public class CustomizationResponse {
    private Long id;
    private String logoUrl;
    private String themeName;
    private String aboutText;
}
