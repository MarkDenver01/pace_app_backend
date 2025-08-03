package io.pace.backend.controller;


import io.pace.backend.data.entity.Student;
import io.pace.backend.domain.request.LoginRequest;
import io.pace.backend.domain.request.StudentRequest;
import io.pace.backend.domain.response.LoginResponse;
import io.pace.backend.domain.response.StudentResponse;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.student.StudentService;
import io.pace.backend.service.user_details.CustomizedUserDetails;
import io.pace.backend.service.user_login.UserService;
import io.pace.backend.utils.AuthUtil;
import io.pace.backend.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    StudentService studentService;

    @GetMapping("/student/pending")
    public ResponseEntity<List<Student>> getPendingStudents() {
        return ResponseEntity.ok(studentService.getPendingStudents());
    }

    @PostMapping("/student/approve")
    public ResponseEntity<Student> approveStudent(@RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.approveStudent(request));
    }
}
