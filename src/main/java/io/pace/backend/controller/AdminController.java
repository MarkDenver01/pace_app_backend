package io.pace.backend.controller;


import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.Role;
import io.pace.backend.domain.model.entity.Student;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.User;
import io.pace.backend.domain.model.request.RegisterRequest;
import io.pace.backend.domain.model.response.CustomizationResponse;
import io.pace.backend.domain.model.response.MessageResponse;
import io.pace.backend.domain.model.response.StudentListResponse;
import io.pace.backend.domain.model.response.StudentResponse;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.customization.CustomizationService;
import io.pace.backend.service.user_login.UserService;
import io.pace.backend.utils.AuthUtil;
import io.pace.backend.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping ("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
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
    UniversityRepository universityRepository;

    @Autowired
    UserService userService;

    @Autowired
    CustomizationService customizationService;

    @Autowired
    CourseService courseService;

    @Autowired
    AuthUtil authUtil;

    @GetMapping("/api/students/pending")
    public ResponseEntity<StudentListResponse> getPendingStudents() {
        List<Student> students = userService.getPendingStudents();

        List<StudentResponse> studentResponses = students.stream()
                .map(student -> new StudentResponse(
                        student.getStudentId(),
                        student.getUserName(),
                        student.getEmail(),
                        student.getRequestedDate(),
                        student.getUserAccountStatus(),
                        student.getUniversity().getUniversityId(),
                        student.getUniversity().getUniversityName()
                ))
                .toList();

        return ResponseEntity.ok(new StudentListResponse(
                studentResponses.size(),
                studentResponses
        ));
    }

    @GetMapping("/api/students/approved")
    public ResponseEntity<StudentListResponse> getApprovedStudents() {
        List<Student> students = userService.getApprovedStudents();

        List<StudentResponse> studentResponses = students.stream()
                .map(student -> new StudentResponse(
                        student.getStudentId(),
                        student.getUserName(),
                        student.getEmail(),
                        student.getRequestedDate(),
                        student.getUserAccountStatus(),
                        student.getUniversity().getUniversityId(),
                        student.getUniversity().getUniversityName()
                ))
                .toList();

        return ResponseEntity.ok(new StudentListResponse(
                studentResponses.size(),
                studentResponses
        ));
    }

    @GetMapping("/api/get_all_students")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        List<Student> students = userService.getAllStudents();

        List<StudentResponse> studentResponses = students.stream()
                .map(student -> new StudentResponse(
                        student.getStudentId(),
                        student.getUserName(),
                        student.getEmail(),
                        student.getRequestedDate(),
                        student.getUserAccountStatus(),
                        student.getUniversity().getUniversityId(),
                        student.getUniversity().getUniversityName()
                ))
                .toList();
        return ResponseEntity.ok(studentResponses);
    }

    @PostMapping("/api/student_approve")
    public ResponseEntity<?> approveStudent(@RequestParam("email") String email,
                                            @RequestParam("user_account_status") AccountStatus accountStatus) {
        Student student =  userService.approvedStudent(email, accountStatus);
        return ResponseEntity.ok(new MessageResponse("Student '"
                + student.getEmail() + "' has been approved"));
    }

    @PostMapping(value = "/api/save_themes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCustomization(
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam("theme") String theme,
            @RequestParam("aboutText") String aboutText
    ) {
        try {
            CustomizationResponse updated = customizationService.updateCustomization(logo, theme, aboutText);
            return ResponseEntity.ok(updated);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "File upload failed", "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Error updating theme", "error", e.getMessage()));
        }
    }
}
