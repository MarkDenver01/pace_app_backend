package io.pace.backend.controller;

import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.Role;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.User;
import io.pace.backend.domain.model.request.AnsweredQuestionRequest;
import io.pace.backend.domain.model.request.LoginRequest;
import io.pace.backend.domain.model.request.RegisterRequest;
import io.pace.backend.domain.model.response.*;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.course.CourseRecommendationService;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.customization.CustomizationService;
import io.pace.backend.service.questions.QuestionService;
import io.pace.backend.service.university.UniversityService;
import io.pace.backend.service.user_details.CustomizedUserDetails;
import io.pace.backend.service.user_login.UserService;
import io.pace.backend.utils.AuthUtil;
import io.pace.backend.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    UniversityRepository universityRepository;

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    UniversityService universityService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseRecommendationService courseRecommendationService;

    @Autowired
    CustomizationService customizationService;

    @GetMapping("/public/get_themes")
    public ResponseEntity<?> getCustomization() {
        return ResponseEntity.ok(customizationService.getCustomization());
    }

    @PutMapping("/public/update-password/{id}")
    public ResponseEntity<?> updatePassword(@PathVariable("id") Long id, @RequestParam String newPassword) {
        try {
            userService.updatePassword(id, newPassword);
            return ResponseEntity.ok().body("success");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/public/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", e.getMessage());
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }

        // set the authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomizedUserDetails customizedUserDetails = (CustomizedUserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateToken(customizedUserDetails);

        // get specific role from customized user details
        String role = customizedUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse(null);

        Optional<User> optionalUser = userService.findByEmail(customizedUserDetails.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found", "status", false));
        }

        User user= optionalUser.get();
        LoginResponse loginResponse = null;

        if ("USER".equals(role)) {
            StudentResponse studentResponse = null;
            if (user.getStudent() != null) {
                studentResponse = new StudentResponse(
                        user.getStudent().getStudentId(),
                        user.getStudent().getUserName(),
                        user.getStudent().getEmail(),
                        user.getStudent().getRequestedDate(),
                        user.getStudent().getUserAccountStatus(),
                        user.getUniversity().getUniversityId(),
                        user.getUniversity().getUniversityName()
                );

                loginResponse = new LoginResponse(
                        jwtToken,
                        user.getUserName(),
                        role,
                        studentResponse);
            }
        } else if ("ADMIN".equals(role)) {
            AdminResponse adminResponse = null;
            if (user.getAdmin() != null) {
                adminResponse = new AdminResponse(
                        user.getAdmin().getAdminId(),
                        user.getAdmin().getUserName(),
                        user.getAdmin().getEmail(),
                        user.getAdmin().getCreatedDate(),
                        user.getAdmin().getUserAccountStatus(),
                        user.getAdmin().getUniversity().getUniversityId(),
                        user.getAdmin().getUniversity().getUniversityName(),
                        user.getUserId()
                );
            }

            loginResponse = new LoginResponse(adminResponse, role, user.getUserName(), jwtToken);
        } else {
            loginResponse = new LoginResponse(jwtToken, user.getUserName(), role);
        }

        // return the response entity with JWT token included in the response body
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // check if university is exist
        University university = universityRepository.findById(Math.toIntExact(registerRequest.getUniversityId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "University not found"
                ));

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("already exist"));
        }

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        // Assign the university
        user.setUniversity(university);

        Set<String> tempRoles = registerRequest.getRoles();
        Role role;

        if (tempRoles == null || tempRoles.isEmpty()) {
            role = roleRepository.findRoleByRoleState(RoleState.USER)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.USER)));
        } else {
            String userRole = tempRoles.iterator().next().toLowerCase();
            switch (userRole) {
                case "admin":
                    role = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                            .orElseGet(() -> roleRepository.save(new Role(RoleState.ADMIN)));
                    break;
                case "super_admin":
                    role = roleRepository.findRoleByRoleState(RoleState.SUPER_ADMIN)
                            .orElseGet(() -> roleRepository.save(new Role(RoleState.SUPER_ADMIN)));
                    break;
                case "user":
                default:
                    role = roleRepository.findRoleByRoleState(RoleState.USER)
                            .orElseGet(() -> roleRepository.save(new Role(RoleState.USER)));
            }
            user.setSignupMethod("email");
        }

        user.setRole(role);

        // Call this to save both User and Student
        userService.registerUser(user);

        return ResponseEntity.ok(new MessageResponse("success"));
    }


    @GetMapping("/api/get_username")
    public ResponseEntity<?> getUsername(@AuthenticationPrincipal UserDetails userDetails) {
        return (userDetails != null
                ? ResponseEntity.ok(new UsernameResponse(userDetails.getUsername()))
                :  ResponseEntity.badRequest().body(new MessageResponse("user not found")));
    }

    @PostMapping("/public/forgot_password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            userService.generatePasswordResetToken(email);
            return ResponseEntity.ok(new MessageResponse("success"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("internal server error"));
        }
    }

    @PostMapping("/api/reset_password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(new MessageResponse("success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    @GetMapping("/api/questions")
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @PostMapping("/api/course_recommended/top3")
    public ResponseEntity<List<CourseMatchResponse>> getRecommendedCourse(@RequestBody List<AnsweredQuestionRequest> answers) {
        List<CourseMatchResponse> results = courseRecommendationService.getTopCourses(answers);
        return ResponseEntity.ok(results);
    }
}
