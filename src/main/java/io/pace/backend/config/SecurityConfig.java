package io.pace.backend.config;

import io.pace.backend.auth.AuthEntryPoint;
import io.pace.backend.auth.AuthTokenFilter;
import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.Role;
import io.pace.backend.domain.model.entity.User;
import io.pace.backend.repository.RoleRepository;
import io.pace.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private AuthEntryPoint unAuthorizedHandler;

    @Autowired
    @Lazy
    private SocialAuthenticationHandler authorizedHandler;

    @Autowired
    private CorsConfig corsConfig;

    /**
     * Filter to handle JWT token in request header.
     */
    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Password encoder used for hashing passwords (BCrypt).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring authentication manager bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Initialize default roles and users (super admin, admin) if not present in DB.
     */
    @Bean
    public CommandLineRunner init(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            Role superAdminRole = roleRepository.findRoleByRoleState(RoleState.SUPER_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.SUPER_ADMIN)));

            Role adminRole = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.ADMIN)));

            if (!userRepository.existsByEmail("pace@superadmin.com")) {
                User user = new User(
                        "Super Administrator",
                        "pace@superadmin.com",
                        passwordEncoder.encode("admin"));
                user.setSignupMethod("email");
                user.setRole(superAdminRole);
                userRepository.save(user);
            }

            if (!userRepository.existsByEmail("pace@admin.com")) {
                User user = new User(
                        "Administrator",
                        "pace@admin.com",
                        passwordEncoder.encode("admin"));
                user.setSignupMethod("email");
                user.setRole(adminRole);
                userRepository.save(user);
            }
        };
    }

    /**
     * Main Spring Security filter chain configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Enable CSRF protection except for these endpoints
//                .csrf(csrf -> csrf
//                        .ignoringRequestMatchers("/user/public/login", "/user/public/register")
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .csrf(csrf -> csrf.disable()) // disable csrf (recommended for APIs with JWT)

                // Enable CORS with custom configuration
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/csrf_token").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/user/public/**").permitAll()
                        .requestMatchers("/user/api/**").authenticated()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/superadmin/**").hasAuthority("SUPER_ADMIN")
                        .anyRequest().authenticated())

                // OAuth2 login handling
                .oauth2Login(oauth -> oauth.successHandler(authorizedHandler))

                // Exception handling (unauthorized access)
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unAuthorizedHandler))

                // Add JWT token filter before the username/password filter
                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)

                // configures Spring Security to not use HTTP sessions at all.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Enable form login and basic auth
                .formLogin(Customizer.withDefaults())
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
