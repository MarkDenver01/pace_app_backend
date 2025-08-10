package io.pace.backend.service.user_login;


import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.UserDomainService;
import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.model.entity.*;
import io.pace.backend.repository.*;
import io.pace.backend.service.email.EmailService;
import jakarta.transaction.Transactional;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.pace.backend.utils.Utils.isStringNullOrEmpty;

@Transactional
@Service
public class UserService implements UserDomainService {
    @Value("${base.url.react}")
    String baseUrl;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    UniversityRepository universityRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    EmailService emailService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public void updateUserRoles(Long userId, String roleState) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        RoleState state = RoleState.valueOf(roleState);
        Role role = roleRepository.findRoleByRoleState(state)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException( "Failed to update password");
        }
    }

    @Override
    public void generatePasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plus(24, ChronoUnit.HOURS);
        PasswordResetToken resetToken = new PasswordResetToken(token, expiryDate, user);
        passwordResetTokenRepository.save(resetToken);

        String resetUrl = baseUrl + "/reset_password?token=" + token;
        // send email to user
        emailService.sendPasswordResetEmail(user.getEmail(), resetUrl);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid password reset token"));

        if (resetToken.isUsed()) {
            throw new RuntimeException("Password reset token is used");
        }

        if (resetToken.getExpiryTime().isBefore(Instant.now())) {
            throw new RuntimeException("Password reset token is expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void registerUser(User user) {
        boolean isExists = userRepository.existsByEmailAndUniversity_UniversityId(
                user.getEmail(),
                user.getUniversity().getUniversityId()
        );

        if (isExists) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User already exists"
            );
        }

        // protect againts SUPER_ADMIN being assigned to a university
        user.assignUniversity(user.getUniversity());

        if (user.getRole() != null && user.getRole().getRoleState() == RoleState.USER) {
            Student student = new Student();
            student.setUserName(user.getUserName());
            student.setEmail(user.getEmail());
            student.setRequestedDate(LocalDateTime.now());
            student.setUserAccountStatus(AccountStatus.PENDING); // pending
            student.setUniversity(user.getUniversity());
            student.setUser(user);

            // encode the password
            if (!isStringNullOrEmpty(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // set student to user as well (bidirectional)
            user.setStudent(student);

            studentRepository.save(student);
            userRepository.save(user);
        } else {
            Admin admin = new Admin();
            admin.setUserName(user.getUserName());
            admin.setEmail(user.getEmail());
            admin.setCreatedDate(LocalDateTime.now());
            admin.setUserAccountStatus(AccountStatus.PENDING); // pending
            admin.setUniversity(user.getUniversity());
            admin.setUser(user);

            // encode the password
            if (!isStringNullOrEmpty(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            // set student to user as well (bidirectional)
            user.setAdmin(admin);

            adminRepository.save(admin);
            userRepository.save(user);
        }

    }

    @Override
    public List<Student> getPendingStudents() {
        return studentRepository.findByUserAccountStatus(AccountStatus.PENDING);
    }

    @Override
    public List<Student> getApprovedStudents() {
        return studentRepository.findByUserAccountStatus(AccountStatus.APPROVED);
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public List<Admin> getAllAdmin() {
        return adminRepository.findAll();
    }

    @Override
    public Student approvedStudent(String email, AccountStatus accountStatus) {
        Student student = studentRepository
                .findByEmailAndUserAccountStatus(email, AccountStatus.PENDING)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        student.setUserAccountStatus(accountStatus);
        return studentRepository.save(student);
    }

    @Override
    public boolean isUniversityExists(Long universityId) {
        return universityRepository.existsUniversityByUniversityId(universityId);
    }

    @Override
    public boolean isExistByEmailAndUniversity(String email, Long universityId) {
        return userRepository.existsByEmailAndUniversity_UniversityId(email, universityId);
    }
}
