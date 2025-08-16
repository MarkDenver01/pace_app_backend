package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customization")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customization_id")
    private Long customizationId;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "theme_name", nullable = false)
    private String themeName;

    @Column(name = "about_text", length = 2000)
    private String aboutText;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;
}
