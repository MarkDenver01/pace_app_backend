package io.pace.backend.controller;

import io.pace.backend.data.entity.Role;
import io.pace.backend.data.entity.User;
import io.pace.backend.data.state.RoleState;
import io.pace.backend.domain.request.AnsweredQuestionRequest;
import io.pace.backend.domain.request.LoginRequest;
import io.pace.backend.domain.request.RegisterRequest;
import io.pace.backend.domain.response.*;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.course.CourseRecommendationService;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.questions.QuestionService;
import io.pace.backend.service.user_details.CustomizedUserDetails;
import io.pace.backend.service.user_login.UserService;
import io.pace.backend.utils.AuthUtil;
import io.pace.backend.utils.JwtUtils;
import jakarta.validation.Valid;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseRecommendationService courseRecommendationService;

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

        // prepare the response body, now including the JWT token directly in the body
        LoginResponse loginResponse = new LoginResponse(
                customizedUserDetails.getUsername(),
                role,
                jwtToken);

        // return the response entity with JWT token included in the response body
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("already exist"));
        }

        // Create new user with encoded password
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        // Default role = USER if roles not provided
        Set<String> tempRoles = registerRequest.getRoles();
        Role role;

        if (tempRoles == null || tempRoles.isEmpty()) {
            role = roleRepository.findRoleByRoleState(RoleState.USER)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleState(RoleState.USER);
                        return roleRepository.save(newRole);
                    });
        } else {
            String userRole = tempRoles.iterator().next().toLowerCase();

            switch (userRole) {
                case "admin":
                    role = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setRoleState(RoleState.ADMIN);
                                return roleRepository.save(newRole);
                            });
                    break;
                case "super_admin":
                    role = roleRepository.findRoleByRoleState(RoleState.SUPER_ADMIN)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setRoleState(RoleState.SUPER_ADMIN);
                                return roleRepository.save(newRole);
                            });
                    break;
                case "user":
                default:
                    role = roleRepository.findRoleByRoleState(RoleState.USER)
                            .orElseGet(() -> {
                                Role newRole = new Role();
                                newRole.setRoleState(RoleState.USER);
                                return roleRepository.save(newRole);
                            });
                    break;
            }

            user.setSignupMethod("email");
        }

        user.setRole(role); // Set single role
        userRepository.save(user);

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
