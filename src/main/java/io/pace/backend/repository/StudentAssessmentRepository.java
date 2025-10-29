package io.pace.backend.repository;

import io.pace.backend.domain.model.entity.StudentAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
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


    @Query("SELECT MIN(sa.createdDateTime), MAX(sa.createdDateTime) " +
            "FROM StudentAssessment sa " +
            "WHERE sa.university.universityId = :universityId")
    List<Object[]> findMinAndMaxCreatedDateByUniversity(@Param("universityId") Long universityId);


    @Query("""
       SELECT FUNCTION('DATE', sa.createdDateTime) AS date, COUNT(sa) AS count
       FROM StudentAssessment sa
       WHERE sa.createdDateTime BETWEEN :startDate AND :endDate
         AND sa.university.universityId = :universityId
       GROUP BY FUNCTION('DATE', sa.createdDateTime)
       ORDER BY FUNCTION('DATE', sa.createdDateTime)
       """)
    List<Object[]> countAssessmentsByDateForUniversity(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("universityId") Long universityId
    );

    @Query("""
       SELECT FUNCTION('DATE', sa.createdDateTime) AS date, COUNT(sa) AS count
       FROM StudentAssessment sa
       WHERE sa.createdDateTime BETWEEN :startDate AND :endDate
         AND sa.university.universityId = :universityId
         AND LOWER(sa.enrollmentStatus) = 'same school'
       GROUP BY FUNCTION('DATE', sa.createdDateTime)
       ORDER BY FUNCTION('DATE', sa.createdDateTime)
       """)
    List<Object[]> countSameSchoolStudentsByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("universityId") Long universityId
    );

    @Query("""
       SELECT FUNCTION('DATE', sa.createdDateTime) AS date, COUNT(sa) AS count
       FROM StudentAssessment sa
       WHERE sa.createdDateTime BETWEEN :startDate AND :endDate
         AND sa.university.universityId = :universityId
         AND LOWER(sa.enrollmentStatus) = 'other school'
       GROUP BY FUNCTION('DATE', sa.createdDateTime)
       ORDER BY FUNCTION('DATE', sa.createdDateTime)
       """)
    List<Object[]> countOtherSchoolStudentsByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("universityId") Long universityId
    );

    @Query("""
       SELECT FUNCTION('DATE', sa.createdDateTime) AS date, COUNT(sa) AS count
       FROM StudentAssessment sa
       WHERE sa.createdDateTime BETWEEN :startDate AND :endDate
         AND sa.university.universityId = :universityId
         AND LOWER(sa.enrollmentStatus) = 'new school'
       GROUP BY FUNCTION('DATE', sa.createdDateTime)
       ORDER BY FUNCTION('DATE', sa.createdDateTime)
       """)
    List<Object[]> countNewSchoolStudentsByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("universityId") Long universityId
    );

    @Query("""
       SELECT rc.courseDescription, COUNT(rc)
       FROM RecommendedCourses rc
       WHERE rc.studentAssessment.university.universityId = :universityId
       GROUP BY rc.courseDescription
       ORDER BY COUNT(rc) DESC
       """)
    List<Object[]> countStudentsPerCourse(@Param("universityId") Long universityId);

    // Get the min and max createdDateTime for competitor (other school) students
    @Query("""
        SELECT MIN(sa.createdDateTime), MAX(sa.createdDateTime)
        FROM StudentAssessment sa
        WHERE sa.university.universityId = :universityId
          AND LOWER(sa.enrollmentStatus) = 'other school'
    """)
    Object[] findMinAndMaxCreatedDateForCompetitors(@Param("universityId") Long universityId);

    // Count competitor students per date and enrolled university
    @Query("""
        SELECT FUNCTION('DATE', sa.createdDateTime) AS date,
               sa.enrolledUniversity AS competitorName,
               COUNT(sa) AS totalCount
        FROM StudentAssessment sa
        WHERE sa.university.universityId = :universityId
          AND sa.createdDateTime BETWEEN :startDate AND :endDate
          AND LOWER(sa.enrollmentStatus) = 'other school'
        GROUP BY FUNCTION('DATE', sa.createdDateTime), sa.enrolledUniversity
        ORDER BY FUNCTION('DATE', sa.createdDateTime), sa.enrolledUniversity
    """)
    List<Object[]> findCompetitorCountsByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("universityId") Long universityId
    );

}
