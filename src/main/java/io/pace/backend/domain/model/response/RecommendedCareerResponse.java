package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecommendedCareerResponse {
    private Long careerId;
    private String careerName;
}