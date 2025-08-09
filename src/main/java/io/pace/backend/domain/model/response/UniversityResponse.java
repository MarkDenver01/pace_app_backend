package io.pace.backend.domain.model.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityResponse {
    private long universityId;
    private String universityName;
}
