package io.pace.backend.controller;


import io.pace.backend.data.entity.University;
import io.pace.backend.domain.request.UniversityRequest;
import io.pace.backend.domain.response.UniversityResponse;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.customization.CustomizationService;
import io.pace.backend.service.university.UniversityService;
import io.pace.backend.service.user_login.UserService;
import io.pace.backend.utils.AuthUtil;
import io.pace.backend.utils.JwtUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/superadmin")
public class SuperAdminController {
    @Autowired
    UniversityService universityService;

    @GetMapping("/api/university/get_all_university")
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

    // HANDLE VALIDATION ERRORS
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
