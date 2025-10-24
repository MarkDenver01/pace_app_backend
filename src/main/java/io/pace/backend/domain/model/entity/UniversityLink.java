package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "university_link",
        uniqueConstraints = @UniqueConstraint(columnNames = "university_id")

)
public class UniversityLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "link_id")
    private Long linkId;

    @Column(name = "token", unique = true, nullable = false)
    private String token;

    @Column(name = "path", nullable = false)
    private String path;

    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "university_id", referencedColumnName = "university_id", unique = true)
    private University university;

    @Column(name = "email_domain")
    private String emailDomain;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();
}
