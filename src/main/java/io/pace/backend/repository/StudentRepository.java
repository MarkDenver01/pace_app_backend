package io.pace.backend.repository;

import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.model.entity.Student;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    List<Student> findByUserAccountStatus(AccountStatus accountStatus); // 0 = pending, 1 = approved

    List<Student> findStudent_ByUniversity_UniversityId(Long universityId);

    Optional<Student> findByEmailAndUserAccountStatus(String email, AccountStatus accountStatus);


    Optional<Student> findByEmailAndUserAccountStatusAndVerificationCode(String email, AccountStatus userAccountStatus, int verificationCode);
}
