package io.pace.backend.repository;

import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUser_UserId(Long userId);
    Optional<Admin> findByAdminId(Long adminId);
    Optional<Admin> findByAdminIdAndUserAccountStatus(Long adminId, AccountStatus accountStatus);
    Optional<Admin> findByEmail(String email);

    List<Admin> findByUserAccountStatus(AccountStatus userAccountStatus);

    Optional<Admin> findByEmailAndUserAccountStatus(String email, AccountStatus userAccountStatus);

}
