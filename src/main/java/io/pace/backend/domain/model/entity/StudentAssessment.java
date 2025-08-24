package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "student_assessment",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "email")
        })
public class StudentAssessment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;

    @NotBlank
    @Size(max = 20)
    @Column(name = "username")
    private String userName;

    @NotBlank
    @Size(max =50)
    @Email
    @Column(name = "email")
    private String email;

    // TODO GET TOP 3 COURSES
    // TODO GET TIME ACTIVE
    // TODO UNIVERSITY
}
