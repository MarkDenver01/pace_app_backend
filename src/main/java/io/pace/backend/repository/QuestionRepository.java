package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.Questions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Questions, Long> {
    List<Questions> findByCourse_CourseId(Long courseId);

    @Query("""
                SELECT q
                FROM Questions q
                JOIN q.course c
                JOIN c.universityCourses uc
                WHERE uc.university.universityId = :universityId
                  AND uc.status = 'Active'
            """)
    List<Questions> findActiveQuestionsByUniversity(Long universityId);
}
