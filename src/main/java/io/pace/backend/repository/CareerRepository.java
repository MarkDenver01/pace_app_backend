package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CareerRepository extends JpaRepository<Career, Long> {
    Optional<Career> findByCareerId(Long careerId);

    Optional<Career> findByCareer_AndCourse_CourseName(String career, String courseName);

    Optional<Career> findByCareerId_AndCourse_CourseName(Long careerId, String courseName);

    List<Career> findByCourse_CourseId(Long courseId);
}
