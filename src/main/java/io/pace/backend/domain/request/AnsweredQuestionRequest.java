package io.pace.backend.domain.request;

import lombok.Data;

@Data
public class AnsweredQuestionRequest {
    private Long questionId;
    private String answer; // "Yes" or "No"
}
