package io.pace.backend.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import io.pace.backend.config.FacebookConfig;
import io.pace.backend.domain.enums.AccountStatus;
import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.*;
import io.pace.backend.domain.model.request.AnsweredQuestionRequest;
import io.pace.backend.domain.model.request.LoginRequest;
import io.pace.backend.domain.model.request.RegisterRequest;
import io.pace.backend.domain.model.request.StudentAssessmentRequest;
import io.pace.backend.domain.model.response.*;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.StudentRepository;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.repository.UserRepository;
import io.pace.backend.service.assessment.AssessmentService;
import io.pace.backend.service.course.CourseRecommendationService;
import io.pace.backend.service.course.CourseService;
import io.pace.backend.service.customization.CustomizationService;
import io.pace.backend.service.link.UniversityLinkService;
import io.pace.backend.service.questions.QuestionService;
import io.pace.backend.service.university.UniversityService;
import io.pace.backend.service.user_details.CustomizedUserDetails;
import io.pace.backend.service.user_login.SocialLoginService;
import io.pace.backend.service.user_login.UserService;
import io.pace.backend.service.user_login.facebook.FacebookAuthService;
import io.pace.backend.service.user_login.google.GoogleAuthService;
import io.pace.backend.service.user_login.instagram.InstagramAuthService;
import io.pace.backend.service.user_login.twitter.TwitterAuthService;
import io.pace.backend.utils.AuthUtil;
import io.pace.backend.utils.JwtUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
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
    StudentRepository studentRepository;

    @Autowired
    UniversityRepository universityRepository;

    @Autowired
    UserService userService;

    @Autowired
    CourseService courseService;

    @Autowired
    UniversityService universityService;

    @Autowired
    UniversityLinkService universityLinkService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private CourseRecommendationService courseRecommendationService;

    @Autowired
    CustomizationService customizationService;

    @Autowired
    GoogleAuthService googleAuthService;

    @Autowired
    FacebookAuthService facebookAuthService;

    @Autowired
    InstagramAuthService instagramAuthService;

    @Autowired
    TwitterAuthService twitterAuthService;

    @Autowired
    SocialLoginService socialLoginService;

    @Value("${dynamic_link_base_url}")
    private String dynamicLinkBaseUrl;
    @Autowired
    private AssessmentService assessmentService;

    @PostMapping("/public/validate-temp-password/{universityId}")
    public ResponseEntity<?> validateTempPassword(
            @PathVariable Long universityId,
            @RequestParam String tempPassword) {

        boolean isValid = userService.validateTempPassword(universityId, tempPassword);

        return ResponseEntity.ok().body(Map.of("valid", isValid));
    }

    @PutMapping("/public/update-password/{universityId}")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long universityId,
            @RequestParam String newPassword) {

        try {
            userService.updatePassword(universityId, newPassword);
            return ResponseEntity.ok().body(Map.of("success", true));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", ex.getMessage()
            ));
        }
    }

    @GetMapping("/public/check/facebook_account")
    public ResponseEntity<?> getFacebookAccount(@RequestParam("accessToken") String accessToken) {
        // get email
        Map<String, Object> userResponse = facebookAuthService.verifyAccessToken(accessToken);
        String email = (String) userResponse.get("email");

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Facebook account did not return email");
        }

        return ResponseEntity.ok(userService.isFacebookAccountExists(email));
    }

    @GetMapping("/public/check/google_account")
    public ResponseEntity<?> getGoogleAccount(@RequestParam("email") String email) {
        return ResponseEntity.ok(userService.isGoogleAccountExists(email));
    }

    @PostMapping("/public/twitter_login")
    public ResponseEntity<?> twitterLogin(
            @RequestParam("accessToken") String accessToken,
            @RequestParam(value = "universityId", required = false) Long universityId
    ) {
        Map<String, Object> userResponse = twitterAuthService.verifyAccessToken(accessToken);
        String email = (String) userResponse.get("email");
        String name = (String) userResponse.get("name");

        LoginResponse loginResponse = socialLoginService.handleLogin(email, name, "twitter", universityId);
        return new ResponseEntity<>(loginResponse,
                userRepository.findByEmail(email).isEmpty() ? HttpStatus.CREATED : HttpStatus.OK);
    }

    @PostMapping("/public/instagram_login")
    public ResponseEntity<?> instagramLogin(
            @RequestParam("accessToken") String accessToken,
            @RequestParam(value = "universityId", required = false) Long universityId
    ) {
        Map<String, Object> userResponse = instagramAuthService.verifyAccessToken(accessToken);
        String email = (String) userResponse.get("email");
        String name = (String) userResponse.get("name");

        LoginResponse loginResponse = socialLoginService.handleLogin(email, name, "instagram", universityId);
        return new ResponseEntity<>(loginResponse,
                userRepository.findByEmail(email).isEmpty() ? HttpStatus.CREATED : HttpStatus.OK);
    }

    @PostMapping("/public/facebook_login")
    public ResponseEntity<?> facebookLogin(
            @RequestParam("accessToken") String accessToken,
            @RequestParam(value = "universityId", required = false) Long universityId
    ) {
        Map<String, Object> userResponse = facebookAuthService.verifyAccessToken(accessToken);
        String email = (String) userResponse.get("email");
        String name = (String) userResponse.get("name");

        if (email == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Facebook account did not return email");
        }

        LoginResponse loginResponse = socialLoginService.handleLogin(email, name, "facebook", universityId);
        return new ResponseEntity<>(loginResponse,
                userRepository.findByEmail(email).isEmpty() ? HttpStatus.CREATED : HttpStatus.OK);
    }

    @PostMapping("/public/google_login")
    public ResponseEntity<?> googleLogin(
            @RequestParam("idToken") String idToken,
            @RequestParam(value = "universityId", required = false) Long universityId) throws Exception {

        GoogleIdToken.Payload payload = googleAuthService.verify(idToken);
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        LoginResponse loginResponse = socialLoginService.handleLogin(email, name, "google", universityId);
        return new ResponseEntity<>(loginResponse,
                userRepository.findByEmail(email).isEmpty() ? HttpStatus.CREATED : HttpStatus.OK);
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

    @GetMapping("/api/questions/all")
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @PostMapping("/api/course_recommended/top3")
    public ResponseEntity<List<CourseMatchResponse>> getRecommendedCourse(@RequestBody List<AnsweredQuestionRequest> answers) {
        List<CourseMatchResponse> results = courseRecommendationService.getTopCourses(answers);

//        if (!results.isEmpty()) {
//            assessmentService.saveRecommendedCourse(studentAssessmentId,
//                    results.get(0).getCourseId()); // or fetch full entity
//        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/public/university/all")
    public ResponseEntity<?> getAllUniversities() {
        try {
            List<UniversityResponse> universities = universityService.getAllUniversities();
            return ResponseEntity.ok(universities);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch universities"));
        }
    }

    @GetMapping("/public/university/select")
    public ResponseEntity<?> getUniversityById(@RequestParam("university_id") Long universityId) {
        try {
            UniversityResponse university = universityService.getUniversity(universityId);
            return ResponseEntity.ok(university); // return a single object, not a list
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/public/user/account/{universityId}")
    public ResponseEntity<Map<String, String>> generateLink(@PathVariable Long universityId) {
        UniversityLink universityLink = universityLinkService.createOrGetLink(Math.toIntExact(universityId));

        String fullLink = dynamicLinkBaseUrl + universityLink.getPath() +"&token="+ universityLink.getToken();
        return ResponseEntity.ok(Map.of(
                "universityId", String.valueOf(universityId),
                "link", fullLink
        ));
    }

    @GetMapping("/public/dynamic_link/{token}")
    public ResponseEntity<UniversityLink> resolveByToken(@PathVariable String token) {
        return universityLinkService.getByToken(token)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/public/generated_dynamic_link/{universityId}")
    public ResponseEntity<String> getGeneratedLink(@PathVariable Long universityId) {
        String fullLink = universityLinkService.getFullLinkByUniversity(universityId);
        return ResponseEntity.ok(fullLink);
    }

    @GetMapping("/public/dynamic_link/token_validation")
    public ResponseEntity<UniversityLinkResponse> validateToken(@RequestParam("token") String token) {
        boolean isValid = universityLinkService.isTokenValid(token);

        if (isValid) {
            return ResponseEntity.ok(new UniversityLinkResponse("success"));
        } else {
            return ResponseEntity.badRequest().body(new UniversityLinkResponse("failed"));
        }
    }

    @GetMapping("/public/customization")
    public ResponseEntity<CustomizationResponse> getTheme(@RequestParam("universityId") Long universityId) {
        CustomizationResponse response = customizationService.getTheme(universityId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/student_assessment/save")
    public ResponseEntity<StudentAssessmentResponse> saveAssessment(@RequestBody StudentAssessmentRequest request) {
        StudentAssessmentResponse response = assessmentService.saveStudentAssessment(request);
        return ResponseEntity.ok(response);
    }

}
