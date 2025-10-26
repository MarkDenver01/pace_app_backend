package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.StudentAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentAssessmentRepository extends JpaRepository<StudentAssessment, Long> {
    long countByUniversity_UniversityId(Long universityId);

    boolean existsByEmail(String email);
}
