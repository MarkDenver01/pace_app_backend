package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.StudentAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentAssessmentRepository extends JpaRepository<StudentAssessment, Long> {
    boolean existsByEmail(String email);

    long countByUniversity_UniversityIdAndRecommendedCourses_CourseId(Long universityId, Long courseId);

    Optional<StudentAssessment> findByUniversity_UniversityIdAndEmail(Long universityUniversityId, String email);

    List<StudentAssessment> findByUniversity_UniversityId(Long universityId);

    Optional<StudentAssessment> findByEmail(String email);

    long countByUniversity_UniversityId(Long universityId);

    // other school, new school, same school
    long countByUniversity_UniversityIdAndEnrollmentStatusIgnoreCase(Long universityId, String enrollmentStatus);



    @Query("""
    SELECT rc.courseDescription, COUNT(sa)
    FROM StudentAssessment sa
    JOIN sa.recommendedCourses rc
    WHERE sa.university.universityId = :universityId
      AND sa.createdDateTime BETWEEN :startDate AND :endDate
    GROUP BY rc.courseDescription
    ORDER BY COUNT(sa) DESC
""")
    List<Object[]> findTopCoursesByDateRange(
            @Param("universityId") Long universityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
        SELECT sa.enrolled_university AS competitorName, COUNT(*) AS totalCount
        FROM student_assessment sa
        WHERE sa.university_id = :universityId
          AND LOWER(sa.enrollment_status) = 'other school'
        GROUP BY sa.enrolled_university
        ORDER BY totalCount DESC
        LIMIT 3
        """, nativeQuery = true)
    List<Object[]> findTop3CompetitorUniversitiesByUniversityId(@Param("universityId") Long universityId);


}
