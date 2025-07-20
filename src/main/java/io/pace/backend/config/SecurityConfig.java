package io.pace.backend.config;

import io.pace.backend.auth.AuthEntryPoint;
import io.pace.backend.auth.AuthTokenFilter;
import io.pace.backend.data.entity.Role;
import io.pace.backend.data.entity.User;
import io.pace.backend.data.state.RoleState;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true,
        jsr250Enabled = true)
public class SecurityConfig {
    @Autowired
    private AuthEntryPoint unAuthorizedHandler;

    @Autowired
    @Lazy
    SocialAuthenticationHandler authorizedHandler;

    @Autowired
    CorsConfig corsConfig;

    @Bean
    public AuthTokenFilter authTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            Role superAdminRole = roleRepository.findRoleByRoleState(RoleState.SUPER_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.SUPER_ADMIN)));

            Role adminRole = roleRepository.findRoleByRoleState(RoleState.ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(RoleState.ADMIN)));


            // for super admin
            if (!userRepository.existsByEmail("pace@superadmin.com")) {
                User user = new User(
                        "Super Administrator",
                        "pace@superadmin.com",
                        passwordEncoder.encode("admin"));
                user.setSignupMethod("email");
                user.setRole(superAdminRole);
                userRepository.save(user);
            }

            // for admin
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

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf ->
                csrf.ignoringRequestMatchers("/user/public/login")
                        .ignoringRequestMatchers("/user/public/register")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));
        http.cors(cors -> corsConfig.corsConfigurationSource());
        http.authorizeHttpRequests((requests)
                        -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/superadmin/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/csrf_token").permitAll()
                        .requestMatchers("/user/public/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oAuth2Login
                        -> oAuth2Login.successHandler(authorizedHandler));
        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(unAuthorizedHandler));
        http.addFilterBefore(authTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);
        http.formLogin(Customizer.withDefaults());
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }


}
