package io.pace.backend.domain.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gmail_tokens")
@Data
@NoArgsConstructor
public class GmailToken {
    @Id
    private Long id = 1L; // only 1 token record

    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private Long expiresIn;

    private Long tokenCreatedAt;
}
