package io.pace.backend.domain.model.response;

import io.pace.backend.domain.model.entity.University;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityResponse {
    private long universityId;
    private String universityName;
}
