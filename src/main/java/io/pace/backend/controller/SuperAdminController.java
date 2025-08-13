package io.pace.backend.controller;


import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.*;
import io.pace.backend.domain.model.request.CourseRequest;
import io.pace.backend.domain.model.request.QuestionRequest;
import io.pace.backend.domain.model.request.RegisterRequest;
import io.pace.backend.domain.model.request.UniversityRequest;
import io.pace.backend.domain.model.response.*;
import io.pace.backend.repository.*;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.email.EmailService;
import io.pace.backend.service.questions.QuestionService;
import io.pace.backend.service.university.UniversityService;
import io.pace.backend.service.user_login.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {
    @Autowired
    UniversityService universityService;

    @Autowired
    CourseService courseService;

    @Autowired
    EmailService emailService;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    AdminRepository adminRepository;
    
    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    @Autowired
    UniversityRepository universityRepository;

    @GetMapping("/api/course/all")
    public ResponseEntity<?> getAllCourses() {
        try {
            List<CourseResponse> courses = courseService.getAllCourses();
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch course"));
        }
    }

    @PostMapping("/api/course/save")
    public ResponseEntity<?> saveCourse(@Valid @RequestBody CourseRequest request) {
        try {
            CourseResponse savedCourse = courseService.saveCourse(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedCourse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PutMapping("/api/course/update/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable("id") Long id,
            @Valid @RequestBody CourseRequest request) {
        CourseResponse updated = courseService.updateCourse(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/api/university/all")
    public ResponseEntity<?> getAllUniversities() {
        try {
            List<UniversityResponse> universities = universityService.getAllUniversities();
            return ResponseEntity.ok(universities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch universities"));
        }
    }


    @PostMapping("/api/university/save")
    public ResponseEntity<?> createUniversity(@Valid @RequestBody UniversityRequest request) {
        try {
            UniversityResponse createdUniversity = universityService.addUniversity(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUniversity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred"));
        }
    }

    @PutMapping("/api/university/update/{id}")
    public ResponseEntity<?> updateUniversity(@PathVariable Long id,
                                              @Valid @RequestBody UniversityRequest request) {
        try {
            UniversityResponse updatedUniversity = universityService.updateUniversity(Math.toIntExact(id), request);
            return ResponseEntity.ok(updatedUniversity);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update university"));
        }
    }

    @DeleteMapping("/api/university/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            universityService.deleteUniversity(Math.toIntExact(id));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete university"));
        }
    }

    @PostMapping("/api/admin_account/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        University university = universityRepository.findById(Math.toIntExact(registerRequest.getUniversityId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "University not found"
                ));

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("already exist"));
        }

        emailService.sendTemporaryPassword(
                registerRequest.getEmail(),
                registerRequest.getPassword());

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        // Assign the university
        user.setUniversity(university);

        Set<String> tempRoles = registerRequest.getRoles();
        Role role;

        if (tempRoles == null || tempRoles.isEmpty()) {
            role = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.ADMIN)));
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
                    break;
            }
            user.setSignupMethod("email");
        }

        user.setRole(role);

        // Call this to save both User and Student
        userService.registerUser(user);

        return ResponseEntity.ok(new MessageResponse("success"));
    }

    @PutMapping("/admin_account/{id}/status")
    public ResponseEntity<?> updateAccountStatus(@PathVariable Long id, @RequestParam String status) {


        return adminRepository.findById(id)
                .map(admin -> {
                    try {
                        AccountStatus newStatus = AccountStatus.valueOf(status.toUpperCase());
                        admin.setUserAccountStatus(newStatus);
                        adminRepository.save(admin);
                        return ResponseEntity.ok("Account status updated to " + newStatus);
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Invalid status. Use ACTIVE or INACTIVE");
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/admin_account/{id}/status")
    public ResponseEntity<?> updateAdminStatus(@PathVariable Long id) {
        Optional<Admin> admin = userService.findAdminById(id);

        if (admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin account not found");
        }

        Admin request = admin.get();
        AccountStatus currentStatus = request.getUserAccountStatus();

        if (currentStatus == AccountStatus.VERIFIED || currentStatus == AccountStatus.ACTIVATE) {
            request.setUserAccountStatus(AccountStatus.DEACTIVATE);
        } else if (currentStatus == AccountStatus.DEACTIVATE) {
            request.setUserAccountStatus(AccountStatus.ACTIVATE);
        }

        adminRepository.save(request);

        return ResponseEntity.ok(new AdminResponse(
                request.getAdminId(),
                request.getUserName(),
                request.getEmail(),
                request.getCreatedDate(),
                request.getUserAccountStatus(),
                request.getUniversity().getUniversityId(),
                request.getUniversity().getUniversityName(),
                request.getUser().getUserId()
        ));

    }

    @GetMapping("/api/admin_account/list")
    public ResponseEntity<List<AdminResponse>> getAllAdmins() {
        List<Admin> admins = userService.getAllAdmin();

        List<AdminResponse> adminResponses = admins.stream()
                .map(admin -> new AdminResponse(
                        admin.getAdminId(),
                        admin.getUserName(),
                        admin.getEmail(),
                        admin.getCreatedDate(),
                        admin.getUserAccountStatus(),
                        admin.getUniversity().getUniversityId(),
                        admin.getUniversity().getUniversityName(),
                        admin.getUser().getUserId()
                ))
                .toList();
        return ResponseEntity.ok(adminResponses);
    }

    @GetMapping("/api/course/count")
    public ResponseEntity<Map<String, Long>> getCourseCount(
            @RequestParam Long universityId,
            @RequestParam(defaultValue = "Active") String status
    ) {
        long count = courseService.getCourseCountByUniversity(universityId, status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/api/questions/all")
    public List<QuestionResponse> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/api/course/active")
    public ResponseEntity<List<CourseResponse>> getActiveCoursesByUniversity(@RequestParam Long universityId) {
        List<Course> activeCourses = courseService.getActiveCoursesByUniversity(universityId);

        // Map to CourseResponse DTO
        List<CourseResponse> courseResponses = activeCourses.stream()
                .map(course -> new CourseResponse(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getCourseDescription(),
                        course.getStatus(),
                        course.getUniversity().getUniversityName(),
                        course.getUniversity().getUniversityId()
                ))
                .toList();
        return ResponseEntity.ok(courseResponses);
    }

    @GetMapping("/api/questions/byCourse/{courseId}")
    public List<QuestionResponse> getByCourse(@PathVariable Long courseId) {
        return questionService.getQuestionsByCourse(courseId);
    }

    @GetMapping("/api/questions/byUniversity/{universityId}")
    public List<QuestionResponse> getByUniversity(@PathVariable Long universityId) {
        return questionService.getQuestionsByUniversity(universityId);
    }

    @PostMapping("/api/questions/save")
    public ResponseEntity<?> saveQuestion(@Validated @RequestBody QuestionRequest request) {
        try {
            Questions saved = questionService.saveQuestion(request);
            return ResponseEntity.ok("Question saved with ID: " + saved.getQuestionId());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save question");
        }
    }

    @PutMapping("/api/questions/update/{id}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request
    ) {
        Optional<Questions> optionalQuestion = questionRepository.findById(id);
        if (optionalQuestion.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Questions question = optionalQuestion.get();

        var courseOpt = courseRepository.findById(request.getCourseId());
        if (courseOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid courseId");
        }

        question.setCourse(courseOpt.get());
        question.setCategory(request.getCategory());
        question.setQuestion(request.getQuestion());

        questionRepository.save(question);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/questions/delete/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id) {
        if (!questionRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        questionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // HANDLE VALIDATION ERRORS
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
