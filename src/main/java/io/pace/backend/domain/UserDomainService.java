package io.pace.backend.domain;

import io.pace.backend.data.entity.Role;
import io.pace.backend.data.entity.User;

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
}
