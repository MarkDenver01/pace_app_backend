package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @NotBlank
    @Size(max =50)
    @Column(name = "enrollment_status")
    private String enrollmentStatus; // others, same school

    @NotBlank
    @Size(max =50)
    @Column(name = "enrolled_university")
    private String enrolledUniversity;


    @Column(name = "created_date")
    private String createdDateTime;

    @Column(name = "assessment_status")
    private String assessmentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @OneToMany(mappedBy = "studentAssessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RecommendedCourses> recommendedCourses = new ArrayList<>();
}
