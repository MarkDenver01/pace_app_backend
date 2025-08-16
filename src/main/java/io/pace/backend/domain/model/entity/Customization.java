package io.pace.backend.domain.model.entity;

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
    private Long id;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "theme_name", nullable = false)
    private String themeName;

    @Column(name = "about_text", length = 2000)
    private String aboutText;
}
