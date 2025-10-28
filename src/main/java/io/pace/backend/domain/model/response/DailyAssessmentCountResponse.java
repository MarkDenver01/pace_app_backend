package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailyAssessmentCountResponse {
    private String date;   // formatted as yyyy-MM-dd
    private int count;
}
