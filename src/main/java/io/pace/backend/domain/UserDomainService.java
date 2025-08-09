package io.pace.backend.domain;

import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.model.entity.Role;
import io.pace.backend.domain.model.entity.Student;
import io.pace.backend.domain.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDomainService {
    List<User> getAllUsers();

    List<Role> getAllRoles();

    void updateUserRoles(Long userId, String roleState);

    void updatePassword(Long userId, String newPassword);

    void generatePasswordResetToken(String email);

    void resetPassword(String token, String newPassword);

    Optional<User> findByEmail(String email);

    void registerUser(User user);

    List<Student> getPendingStudents();

    List<Student> getApprovedStudents();

    List<Student> getAllStudents();

    Student approvedStudent(String email, AccountStatus accountStatus);
}
