package io.pace.backend.service.university;

import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.request.UniversityRequest;
import io.pace.backend.domain.model.response.UniversityResponse;
import io.pace.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UniversityService {

    @Autowired
    private UniversityRepository universityRepository;

    public List<UniversityResponse> getAllUniversities(){
        return universityRepository.findAll().stream()
                .map(u -> new UniversityResponse(
                        u.getUniversityId(),
                        u.getUniversityName()
                ))
                .collect(Collectors.toList());
    }

    public UniversityResponse addUniversity(UniversityRequest universityRequest) {University university = new University();
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
                .orElseThrow(() -> new RuntimeException("University not found with id: " + id));

        return new UniversityResponse(updated.getUniversityId(), updated.getUniversityName());
    }

    public void deleteUniversity(int id) {
        if (!universityRepository.existsById(id)) {
            throw new RuntimeException("University not found with id: " + id);
        }
        universityRepository.deleteById(id);
    }
}
