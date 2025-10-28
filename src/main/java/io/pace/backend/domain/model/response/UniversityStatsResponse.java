package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UniversityStatsResponse {
    private Long universityId;
    private long totalAssessments;
    private long totalSameSchool;
    private long totalOtherSchool;
    private long totalNewSchool;
}
