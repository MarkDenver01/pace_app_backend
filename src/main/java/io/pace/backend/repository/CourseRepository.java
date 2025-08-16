package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByUniversity_UniversityId(Long universityId);
    List<Course> findByCourseNameContainingIgnoreCase(String courseName);

    List<Course> findAllByStatus(String status);

    List<Course> findByStatusIgnoreCase(String status);

    List<Course> findByUniversity_UniversityIdAndStatus(Long universityId, String status);

    List<Course> findByUniversity_UniversityIdAndStatusIgnoreCase(Long universityId, String status);

    long countByUniversity_UniversityIdAndStatus(Long universityId, String status);
}
