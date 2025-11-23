package io.pace.backend.service.link;

import io.pace.backend.domain.model.entity.Admin;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.UniversityLink;
import io.pace.backend.domain.model.entity.User;
import io.pace.backend.domain.model.request.UpdateUniversityInfoRequest;
import io.pace.backend.repository.AdminRepository;
import io.pace.backend.repository.UniversityLinkRepository;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UniversityLinkService {
    @Autowired
    UniversityRepository universityRepository;

    @Autowired
    UniversityLinkRepository universityLinkRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    private final SecureRandom random = new SecureRandom();

    @Value("${app.base.frontend.installed}")
    private String installedBaseUrl;

    @Value("${app.base.frontend.notinstalled}")
    private String notInstalledBaseUrl;

    public UniversityLink getOrCreateLink(Long universityId) {
        // Find the university (must exist)
        University university = universityRepository.findByUniversityId(universityId)
                .orElseThrow(() -> new IllegalArgumentException("University not found"));

        // Try to find existing link (only one per university)
        Optional<UniversityLink> existingLink = universityLinkRepository.findByUniversity_UniversityId(universityId);

        if (existingLink.isPresent()) {
            // if already exists, return it directly
            return existingLink.get();
        }

        // Otherwise, create new link (first-time only)
        Admin admin = adminRepository.findByUniversity_UniversityId(universityId).orElse(null);
        String emailDomain = admin != null ? admin.getEmailDomain() : "@gmail.com";

        UniversityLink newLink = new UniversityLink();
        newLink.setUniversity(university);
        newLink.setPath("/app-link");
        newLink.setToken(generateToken(10));
        newLink.setEmailDomain(emailDomain);
        return universityLinkRepository.save(newLink);
    }

    public void updateUniversityDetails(UpdateUniversityInfoRequest req) {

        if (req.getUniversityId() == null) {
            throw new RuntimeException("University ID is required");
        }

        // 1. Load University
        University university = universityRepository.findByUniversityId(req.getUniversityId())
                .orElseThrow(() -> new RuntimeException("University not found"));

        // 2. Update University Name
        if (req.getUniversityName() != null && !req.getUniversityName().isBlank()) {
            university.setUniversityName(req.getUniversityName());
            universityRepository.save(university);
        }

        // 3. Update Domain Email for ADMIN + UNIVERSITY LINK
        if (req.getDomainEmail() != null && !req.getDomainEmail().isBlank()) {

            Admin admin = adminRepository.findByUniversity_UniversityId(req.getUniversityId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            admin.setEmailDomain(req.getDomainEmail());
            adminRepository.save(admin);

            universityLinkRepository.findByUniversity_UniversityId(req.getUniversityId())
                    .ifPresent(link -> {
                        link.setEmailDomain(req.getDomainEmail());
                        universityLinkRepository.save(link);
                    });
        }

        // 4. OPTIONAL - Change Password
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {

            if (!req.getNewPassword().equals(req.getConfirmPassword())) {
                throw new RuntimeException("New password and confirm password do not match");
            }

            Admin admin = adminRepository.findByUniversity_UniversityId(req.getUniversityId())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            User user = admin.getUser();
            if (user == null) {
                throw new RuntimeException("Admin is not linked to a user account");
            }

            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
            userRepository.save(user);
        }
    }


    public boolean validateToken(Long universityId, String token) {
        return universityLinkRepository.findByUniversityUniversityIdAndToken(universityId, token)
                .isPresent();
    }

    public UniversityLink updateToken(Long universityId) {
        UniversityLink link = universityLinkRepository.findByUniversityUniversityId(universityId)
                .orElseThrow(() -> new IllegalArgumentException("Dynamic link not found for this university"));
        link.setToken(generateToken(10));
        return universityLinkRepository.save(link);
    }

    public String getDynamicLink(String token, boolean isAppInstalled) {
        UniversityLink link = universityLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (isAppInstalled) {
            return String.format("%s/university?universityId=%d&token=%s",
                    installedBaseUrl, link.getUniversity().getUniversityId(), token);
        } else {
            return notInstalledBaseUrl; // redirect to APK downloads page
        }
    }

    public String getEmailDomain(Long universityId) {
        Optional<UniversityLink> link = universityLinkRepository.findByUniversity_UniversityId(universityId);
        return link.isPresent() ? link.get().getEmailDomain() : "";
    }

    private String generateToken(int len) {
        final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        return random.ints(len, 0, chars.length())
                .mapToObj(i -> String.valueOf(chars.charAt(i)))
                .collect(Collectors.joining());
    }
}
