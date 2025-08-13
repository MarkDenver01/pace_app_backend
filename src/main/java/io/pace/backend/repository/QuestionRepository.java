package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Questions, Long> {
    List<Questions> findByCourse_CourseId(Long courseId);

    List<Questions> findByCourse_University_UniversityId(Long universityId);
}
