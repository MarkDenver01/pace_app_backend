package io.pace.backend.repository;

import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.model.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);

    List<Student> findByUserAccountStatus(AccountStatus accountStatus); // 0 = pending, 1 = approved

    Optional<Student> findByEmailAndUserAccountStatus(String email, AccountStatus accountStatus);
}
