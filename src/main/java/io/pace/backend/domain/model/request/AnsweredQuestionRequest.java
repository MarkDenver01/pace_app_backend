package io.pace.backend.domain.model.request;

import lombok.Data;

@Data
public class AnsweredQuestionRequest {
    private Long questionId;
    private String answer; // "Yes" or "No"
}
