package io.pace.backend.service.link;

import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.UniversityLink;
import io.pace.backend.repository.UniversityLinkRepository;
import io.pace.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

    public UniversityLink createOrGetLink(Integer universityId) {
        return universityLinkRepository.findByUniversityUniversityId(Long.valueOf(universityId))
                .orElseGet(() -> {
                    University university = universityRepository.findById(universityId)
                            .orElseThrow(() -> new IllegalArgumentException("University not found"));

                    UniversityLink link = new UniversityLink();
                    link.setUniversity(university);
                    link.setPath("/users/account/universityId=" + universityId);
                    link.setToken(generateToken(12));
                    return universityLinkRepository.save(link);
                });
    }

    public Optional<UniversityLink> getByToken(String token) {
        return universityLinkRepository.findByToken(token);
    }

    public String getFullLinkByUniversity(Long universityId) {
        UniversityLink link = universityLinkRepository.findByUniversity_UniversityId(universityId);
        if (link == null) {
            throw new RuntimeException("No link found for universityId: " + universityId);
        }

        // Ensure valid URL query string
        return String.format(
                "http://localhost%s?universityId=%d&token=%s",
                link.getPath(),                           // e.g., /users/account
                link.getUniversity().getUniversityId(), // e.g., 1
                link.getToken()                           // e.g., c7e2fxvwb7af
        );
    }

    private String generateToken(int len) {
        final String chars =  "abcdefghijklmnopqrstuvwxyz0123456789";
        return random.ints(len, 0, chars.length())
                .mapToObj(i -> String.valueOf(chars.charAt(i)))
                .collect(Collectors.joining());
    }

    public boolean isTokenValid(String token) {
        return universityLinkRepository.findByToken(token).isPresent();
    }
}
