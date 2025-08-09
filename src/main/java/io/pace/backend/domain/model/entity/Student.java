package io.pace.backend.domain.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.pace.backend.domain.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "student_request",
uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Long studentId;


    @NotBlank
    @Size(max = 20)
    @Column(name = "username")
    private String userName;

    @NotBlank
    @Size(max =50)
    @Email
    @Column(name = "email")
    private String email;

    @Column(name = "requested_date")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "MMM dd, yyyy hh:mm a",
            timezone = "Asia/Manila" // or GMT+8
    )
    private LocalDateTime requestedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_account_status", length = 20)
    private AccountStatus userAccountStatus;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @JsonIgnore
    private User user;
}
