package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomizationResponse {
    private String themeName;
    private String logoUrl;
    private String aboutText;
}
