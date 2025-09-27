package io.pace.backend.domain.model.entity;

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
@Table(name = "university")
public class University {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "university_id")
    private Long universityId;

    @NotBlank(message = "University is required")
    @Column(name = "university_name", nullable = false)
    private String universityName;

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL)
    private List<Admin> admins = new ArrayList<>();

    @OneToOne(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private Customization customization;

    @OneToOne(mappedBy = "university", cascade = CascadeType.ALL, orphanRemoval = true)
    private UniversityLink universityLink;
}
