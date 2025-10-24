package io.pace.backend.service.link;

import io.pace.backend.domain.model.entity.Admin;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.UniversityLink;
import io.pace.backend.repository.AdminRepository;
import io.pace.backend.repository.UniversityLinkRepository;
import io.pace.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
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

    private final SecureRandom random = new SecureRandom();

    @Value("${app.base.frontend.installed}")
    private String installedBaseUrl;

    @Value("${app.base.frontend.notinstalled}")
    private String notInstalledBaseUrl;

    public UniversityLink getOrCreateLink(Long universityId) {
        University university = universityRepository.findById(Math.toIntExact(universityId))
                .orElseThrow(() -> new IllegalArgumentException("University not found"));

        Admin admin = adminRepository.findByUniversity_UniversityId(universityId).orElse(null);
        String emailDomain = admin != null ? admin.getEmailDomain() : "@gmail.com";

        return universityLinkRepository.findByUniversityUniversityId(universityId)
                .orElseGet(() -> {
                    UniversityLink newLink = new UniversityLink();
                    newLink.setUniversity(university);
                    newLink.setPath("/app-link");
                    newLink.setToken(generateToken(10));
                    newLink.setEmailDomain(emailDomain);
                    return universityLinkRepository.save(newLink);
                });
    }

    public boolean isUniversityLinkExistEmailDomain(Long universityId, String emailDomain) {
        return universityLinkRepository.existsByUniversity_UniversityIdAndEmailDomainIsNotNullAndEmailDomainNot(
                universityId, emailDomain
        );
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
        UniversityLink link = universityLinkRepository.findByUniversity_UniversityId(universityId);
        return link.getEmailDomain();
    }

    private String generateToken(int len) {
        final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        return random.ints(len, 0, chars.length())
                .mapToObj(i -> String.valueOf(chars.charAt(i)))
                .collect(Collectors.joining());
    }
}
