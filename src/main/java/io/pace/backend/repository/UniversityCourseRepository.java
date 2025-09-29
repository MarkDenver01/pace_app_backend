package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.UniversityCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityCourseRepository extends JpaRepository<UniversityCourse, Long> {
    List<UniversityCourse> findByUniversity(University university);

    Optional<UniversityCourse> findByUniversityAndCourse(University university, Course course);

    long countByUniversity_UniversityIdAndStatus(Long universityId, String status);
    List<UniversityCourse> findByUniversity_UniversityIdAndStatus(Long universityId, String status);

}
