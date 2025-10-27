package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recommended_courses")
public class RecommendedCourses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommended_course_id")
    private Long courseId;

    @NotBlank(message = "Career description is required")
    @Column(name = "course_description", nullable = false)
    private String courseDescription;

    @Column(name = "assessment_result", nullable = false)
    private double assessmentResult;

    @Column(name = "result_description", nullable = false)
    private String resultDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    @JsonBackReference
    private StudentAssessment studentAssessment;

    @OneToMany(mappedBy = "recommendedCourses", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<RecommendedCareer> careers = new ArrayList<>();
}
