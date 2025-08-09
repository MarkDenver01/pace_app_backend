package io.pace.backend.domain.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customization")
@Getter
@Setter
@NoArgsConstructor
public class Customization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "theme_name", nullable = false)
    private String themeName;

    @Column(name = "about_text")
    private String aboutText;
}
