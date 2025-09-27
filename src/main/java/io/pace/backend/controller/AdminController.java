package io.pace.backend.controller;


import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.*;
import io.pace.backend.domain.model.request.CustomizationRequest;
import io.pace.backend.domain.model.request.RegisterRequest;
import io.pace.backend.domain.model.response.*;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.customization.CustomizationService;
import io.pace.backend.service.email.EmailService;
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
    EmailService emailService;

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
        Student student = userService.approvedStudent(email, accountStatus);

        // send an email for indication or reminder that the account has been verified
        emailService.sendEmail(
                email,
                student.getUserName());

        return ResponseEntity.ok(new MessageResponse("Student '"
                + student.getEmail() + "' has been approved"));
    }

    @GetMapping("/api/course/count")
    public ResponseEntity<Map<String, Long>> getCourseCount(@RequestParam(defaultValue = "Active") String status) {
        long count = courseService.getCourseCount(status);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/api/course/all/active")
    public ResponseEntity<List<CourseResponse>> getActiveCoursesByUniversity() {
        List<Course> activeCourses = courseService.getAllActiveCourses("Active");

        // Map to CourseResponse DTO
        List<CourseResponse> courseResponses = activeCourses.stream()
                .map(course -> new CourseResponse(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getCourseDescription(),
                        course.getStatus()
                ))
                .toList();
        return ResponseEntity.ok(courseResponses);
    }

    @PutMapping("api/courses/{courseId}/activate")
    public ResponseEntity<String> activateCourse(
            @PathVariable Long courseId,
            @RequestParam Long universityId) {

        courseService.updateCourseStatus(courseId, universityId, "Active");
        return ResponseEntity.ok("Course activated for university " + universityId);
    }

    @PutMapping(".api/courses/{courseId}/deactivate")
    public ResponseEntity<String> deactivateCourse(
            @PathVariable Long courseId,
            @RequestParam Long universityId) {

        courseService.updateCourseStatus(courseId, universityId, "Inactive");
        return ResponseEntity.ok("Course deactivated for university " + universityId);
    }

    @PostMapping(path = "/api/customization/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CustomizationResponse> saveOrUpdateTheme(@ModelAttribute CustomizationRequest request) throws IOException {
        CustomizationResponse response = customizationService.saveOrUpdateTheme(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/customization/{universityId}")
    public ResponseEntity<CustomizationResponse> getTheme(@PathVariable Long universityId) {
        CustomizationResponse response = customizationService.getTheme(universityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/course/active/all")
    public ResponseEntity<List<CourseResponse>> getAllActiveCourses() {
        List<Course> activeCourses = courseService.getAllActiveCourses("Active");

        List<CourseResponse> courseResponses = activeCourses.stream()
                .map(course -> new CourseResponse(
                        course.getCourseId(),
                        course.getCourseName(),
                        course.getCourseDescription(),
                        course.getStatus()
                ))
                .toList();
        return ResponseEntity.ok(courseResponses);
    }

}



