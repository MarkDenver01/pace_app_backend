package io.pace.backend.domain.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "password_reset_token",
        indexes = {
                @Index(name = "idx_password_reset_token_token", columnList = "token", unique = true)
        }
)
@Data
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String token;       // raw UUID token stored here (NOT encrypted)

    @Column(nullable = false)
    private Instant expiryTime;

    @Column(nullable = false)
    private boolean used = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public PasswordResetToken(String token, Instant expiryTime, User user) {
        this.token = token;
        this.expiryTime = expiryTime;
        this.user = user;
        this.used = false;
    }
}
