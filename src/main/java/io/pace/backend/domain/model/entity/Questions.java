package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.pace.backend.domain.enums.QuestionCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "questions")
public class Questions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnoreProperties({"questions"}) // Prevent recursion
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionCategory category;

    @NotBlank(message = "Question is required")
    @Column(name = "question", nullable = false)
    private String question;
}
