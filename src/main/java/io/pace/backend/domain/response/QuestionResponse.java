package io.pace.backend.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
public class QuestionResponse {
    private Long questionId;
    private String question;
    private String category;
    private String courseName;
}
