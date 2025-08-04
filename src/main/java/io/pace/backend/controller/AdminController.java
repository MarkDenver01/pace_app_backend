package io.pace.backend.controller;


import io.pace.backend.data.entity.Student;
import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.response.MessageResponse;
import io.pace.backend.domain.response.StudentListResponse;
import io.pace.backend.domain.response.StudentResponse;
import io.pace.backend.domain.response.UsernameResponse;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.user_login.UserService;
import io.pace.backend.utils.AuthUtil;
import io.pace.backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping ("/admin")
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
    UserService userService;

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
                        student.getUserAccountStatus()
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
                        student.getUserAccountStatus()
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
                        student.getUserAccountStatus()
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
}
