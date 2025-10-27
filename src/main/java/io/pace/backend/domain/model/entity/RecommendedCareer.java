package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "recommended_career")
public class RecommendedCareer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommended_career_id")
    private Long careerId;

    @NotBlank(message = "Career name is required")
    @Column(name = "career_name", nullable = false)
    private String career;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_course_id")
    @JsonBackReference
    private RecommendedCourses recommendedCourses;
}

