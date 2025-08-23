package io.pace.backend.service.user_login;

import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.Role;
import io.pace.backend.domain.model.entity.Student;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.User;
import io.pace.backend.domain.model.response.LoginResponse;
import io.pace.backend.domain.model.response.StudentResponse;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.StudentRepository;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SocialLoginService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    UniversityRepository universityRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JwtUtils jwtUtils;

    public LoginResponse handleLogin(String email, String name, String signupMethod, Long universityId) {
        Role defaultRole = roleRepository.findRoleByRoleState(RoleState.USER)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        Optional<User> existingUserOptional = userRepository.findByEmail(email);
        boolean isNewUser = existingUserOptional.isEmpty();

        User user;
        Student student;

        if (isNewUser) {
            if (universityId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "University is required for new users");
            }

            University university = universityRepository.findById(Math.toIntExact(universityId))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid University"));

            user = new User();
            user.setEmail(email);
            user.setUserName(name);
            user.setRole(defaultRole);
            user.setSignupMethod(signupMethod);
            user.setUniversity(university);
            user = userRepository.save(user);

            student = new Student();
            student.setUserName(user.getUserName());
            student.setEmail(user.getEmail());
            student.setRequestedDate(LocalDateTime.now());
            student.setUserAccountStatus(AccountStatus.PENDING);
            student.setUniversity(university);
            student.setUser(user);
            student = studentRepository.save(student);
        } else {
            user = existingUserOptional.get();
            if (universityId != null) {
                University university = universityRepository.findById(Math.toIntExact(universityId))
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid University"));

                user.setUniversity(university);
                user = userRepository.save(user);

                student = studentRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Student not found"));
                student.setUniversity(university);
                student = studentRepository.save(student);
            } else {
                student = studentRepository.findByEmail(email)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Student not found"));
            }
        }

        String jwtToken = jwtUtils.generateToken(email, defaultRole.getRoleState().name());

        StudentResponse studentResponse = new StudentResponse(
                student.getStudentId(),
                student.getUserName(),
                student.getEmail(),
                student.getRequestedDate(),
                student.getUserAccountStatus(),
                student.getUniversity().getUniversityId(),
                student.getUniversity().getUniversityName()
        );

        return new LoginResponse(
                jwtToken,
                user.getUserName(),
                defaultRole.getRoleState().name(),
                studentResponse
        );
    }
}
