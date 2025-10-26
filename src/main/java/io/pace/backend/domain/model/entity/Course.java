package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @NotBlank(message = "Course name is required")
    @Column(name = "course_name", nullable = false)
    private String courseName;

    @NotBlank(message = "Course description is required")
    @Column(name = "course_description", nullable = false)
    private String courseDescription;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("course")
    private List<Questions> questions = new ArrayList<>();

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false)
    private String status;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<UniversityCourse> universityCourses = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Career> careers = new ArrayList<>();
}
