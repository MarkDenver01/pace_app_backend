package io.pace.backend.controller;

import io.pace.backend.data.entity.Role;
import io.pace.backend.data.entity.User;
import io.pace.backend.data.state.RoleState;
import io.pace.backend.domain.request.LoginRequest;
import io.pace.backend.domain.request.RegisterRequest;
import io.pace.backend.domain.response.LoginResponse;
import io.pace.backend.domain.response.MessageResponse;
import io.pace.backend.domain.response.UsernameResponse;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UserRepository;
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
    AuthUtil authUtil;

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
            return ResponseEntity.badRequest().body(new MessageResponse("Email already exists"));
        }

        // create a new user account
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        Set<String> tempRoles = registerRequest.getRoles();
        Role role;

        if (tempRoles == null || tempRoles.isEmpty()) {
            role = roleRepository.findRoleByRoleState(RoleState.USER)
                    .orElseThrow(() -> new RuntimeException("No role found"));
        } else {
            String userRoles = tempRoles.iterator().next();
            switch (userRoles) {
                case "admin":
                    role = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                            .orElseThrow(() -> new RuntimeException("No role found"));
                    break;
                case "super_admin":
                    role = roleRepository.findRoleByRoleState(RoleState.SUPER_ADMIN)
                            .orElseThrow(() -> new RuntimeException("No role found"));
                    break;
                case "user":
                default:
                    role = roleRepository.findRoleByRoleState(RoleState.USER)
                            .orElseThrow(() -> new RuntimeException("No role found"));
                    break;
            }

            user.setSignupMethod("email");
        }
        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @GetMapping("/public/get_username")
    public ResponseEntity<?> getUsername(@AuthenticationPrincipal UserDetails userDetails) {
        return (userDetails != null
                ? ResponseEntity.ok(new UsernameResponse(userDetails.getUsername()))
                :  ResponseEntity.badRequest().body(new MessageResponse("User not found")));
    }

    @PostMapping("/public/forgot_password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            userService.generatePasswordResetToken(email);
            return ResponseEntity.ok(new MessageResponse("Password reset token generated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Internal Server Error"));
        }
    }

    @PostMapping("/public/reset_password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(new MessageResponse("Password reset token updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }
}
