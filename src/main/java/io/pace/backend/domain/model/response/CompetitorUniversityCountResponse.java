package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompetitorUniversityCountResponse {
    private String date;           // yyyy-MM-dd
    private String universityName; // competitor university
}
