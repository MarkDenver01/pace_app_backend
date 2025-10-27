package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.StudentAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAssessmentRepository extends JpaRepository<StudentAssessment, Long> {
    long countByUniversity_UniversityId(Long universityId);

    boolean existsByEmail(String email);

    long countByUniversity_UniversityIdAndRecommendedCourses_CourseId(Long universityId, Long courseId);

    Optional<StudentAssessment> findByUniversity_UniversityIdAndAndEmail(Long universityUniversityId, String email);
}
