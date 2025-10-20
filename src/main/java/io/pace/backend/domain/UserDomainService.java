package io.pace.backend.domain;

import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.model.entity.*;

import java.util.List;
import java.util.Optional;

public interface UserDomainService {
    List<User> getAllUsers();

    List<Role> getAllRoles();

    void updateUserRoles(Long userId, String roleState);

    boolean validateTempPassword(Long universityId, String tempPassword);

    void updatePassword(String email, Long universityId, String newPassword, String emailDomain);

    void generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);

    Optional<User> findByEmail(String email);

    void registerUser(User user);

    List<Student> getPendingStudents();

    List<Student> getApprovedStudents();

    List<Student> getAllStudents();

    List<Admin> getAllAdmin();

    Optional<Admin> findAdminByIdAndStatus(Long adminId, AccountStatus status);

    Optional<Admin> findAdminById(Long adminId);

    Student approvedStudent(String email, AccountStatus accountStatus);

    boolean isUniversityExists(Long universityId);

    boolean isExistByEmailAndUniversity(String email, Long universityId);

    boolean isGoogleAccountExists(String email);

    boolean isFacebookAccountExists(String email);
}
