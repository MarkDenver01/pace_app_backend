package io.pace.backend.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @NotBlank
    @Size(max = 20)
    @Column(name = "requested_date")
    private String requestedDate;

    @NotBlank
    @Size(max = 20)
    @Column(name = "user_account_status")
    private int userAccountStatus;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    public Student(String userName, String email, String requestedDate, int userAccountStatus) {
        this.userName = userName;
        this.email = email;
        this.requestedDate = requestedDate;
        this.userAccountStatus = userAccountStatus;
    }
}
