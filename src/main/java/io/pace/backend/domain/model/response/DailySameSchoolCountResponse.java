package io.pace.backend.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailySameSchoolCountResponse {
    private String date; // yyyy-MM-dd
    private int count;
}
