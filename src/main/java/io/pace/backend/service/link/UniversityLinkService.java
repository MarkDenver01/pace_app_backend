package io.pace.backend.service.link;

import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.UniversityLink;
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

    private final SecureRandom random = new SecureRandom();

    @Value("${app.base.frontend.installed}")
    private String installedBaseUrl;

    @Value("${app.base.frontend.notinstalled}")
    private String notInstalledBaseUrl;

    @Value("${base.url.backend}")
    private String baseUrl;

    public UniversityLink createOrGetLink(Long universityId) {
        return universityLinkRepository.findByUniversityUniversityId(universityId)
                .orElseGet(() -> {
                    University university = universityRepository.findById(Math.toIntExact(universityId))
                            .orElseThrow(() -> new IllegalArgumentException("University not found"));

                    UniversityLink link = new UniversityLink();
                    link.setUniversity(university);
                    link.setToken(generateToken(10));
                    link.setPath("/link/" + link.getToken());

                    return universityLinkRepository.save(link);
                });
    }

    public Optional<UniversityLink> getByToken(String token) {
        return universityLinkRepository.findByToken(token);
    }

    public String getShortLink(Long universityId) {
        UniversityLink link = universityLinkRepository.findByUniversity_UniversityId(universityId);
        if (link == null) throw new RuntimeException("Link not found for universityId: " + universityId);

        return baseUrl + link.getPath();
    }

    public String resolveRedirectUrl(String token) {
        UniversityLink link = universityLinkRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // Optional: you could check user-agent or device info
        boolean isAppInstalled = false; // this logic could be improved later

        if (isAppInstalled) {
            return String.format("%s/university?universityId=%d&token=%s",
                    installedBaseUrl, link.getUniversity().getUniversityId(), token);
        } else {
            return notInstalledBaseUrl; // redirect to APK downloads page
        }
    }

    private String generateToken(int len) {
        final String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        return random.ints(len, 0, chars.length())
                .mapToObj(i -> String.valueOf(chars.charAt(i)))
                .collect(Collectors.joining());
    }
}
