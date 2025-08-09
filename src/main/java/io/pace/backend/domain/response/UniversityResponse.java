package io.pace.backend.domain.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniversityResponse {
    private long universityId;
    private String universityName;
}
