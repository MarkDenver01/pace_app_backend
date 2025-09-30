package io.pace.backend.service.university;

import io.pace.backend.domain.enums.RoleState;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.User;
import io.pace.backend.domain.model.request.UniversityRequest;
import io.pace.backend.domain.model.response.UniversityResponse;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private UserRepository userRepository;

    public UniversityResponse getUniversity(Long universityId) {
        return universityRepository.findByUniversityId(universityId)
                .stream()
                .findFirst() // get the first (and only) match
                .map(u -> new UniversityResponse(
                        u.getUniversityId(),
                        u.getUniversityName()
                ))
                .orElseThrow(() -> new RuntimeException("University not found with id " + universityId));
    }

    public List<UniversityResponse> getAllUniversities(){
        return universityRepository.findAll().stream()
                .map(u -> new UniversityResponse(
                        u.getUniversityId(),
                        u.getUniversityName()
                ))
                .collect(Collectors.toList());
    }

    public UniversityResponse addUniversity(UniversityRequest universityRequest) {
        if (universityRepository.existsByUniversityName(universityRequest.getUniversityName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "University already exists");
        }

        University university = new University();
        university.setUniversityName(universityRequest.getUniversityName());
        University saved = universityRepository.save(university);
        return new UniversityResponse(saved.getUniversityId(), saved.getUniversityName());
    }

    public UniversityResponse updateUniversity(int id, UniversityRequest request) {
        University updated = universityRepository.findById(id)
                .map(existing -> {
                    existing.setUniversityName(request.getUniversityName());
                    return universityRepository.save(existing);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "University not found with id: " + id));

        return new UniversityResponse(updated.getUniversityId(), updated.getUniversityName());
    }

    public void deleteUniversity(int id) {
        if (!universityRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "University not found with id: " + id);
        }
        universityRepository.deleteById(id);
    }

    public UniversityResponse assignUserToUniversity(Long universityId, Long userId) {
        University university = universityRepository.findById(Math.toIntExact(universityId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "University not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        RoleState roleState = user.getRole().getRoleState();
        if (roleState == RoleState.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SUPER_ADMIN cannot be assigned to a University");
        }
        if (roleState != RoleState.ADMIN && roleState != RoleState.USER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only ADMIN and STUDENT can be assigned to a University");
        }

        user.setUniversity(university);
        userRepository.save(user);

        return new UniversityResponse(university.getUniversityId(), university.getUniversityName());
    }
}
