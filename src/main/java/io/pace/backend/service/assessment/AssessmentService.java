package io.pace.backend.service.assessment;

import io.pace.backend.repository.StudentAssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AssessmentService {

    @Autowired
    StudentAssessmentRepository studentAssessmentRepository;

    public Map<String, Long> getAssessmentStats(Long universityId, Long courseId) {
        long total = studentAssessmentRepository.countByUniversity_UniversityId(universityId);
        long assessed = studentAssessmentRepository
                .countByUniversity_UniversityIdAndRecommendedCourse_CourseId(universityId, courseId);

        return Map.of(
                "assessed", assessed,
                "total", total
        );
    }
}
