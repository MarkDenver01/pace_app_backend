package io.pace.backend.service.assessment;

import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.domain.model.entity.StudentAssessment;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.request.CourseRequest;
import io.pace.backend.domain.model.request.StudentAssessmentRequest;
import io.pace.backend.domain.model.response.CourseMatchResponse;
import io.pace.backend.domain.model.response.CourseResponse;
import io.pace.backend.domain.model.response.StudentAssessmentResponse;
import io.pace.backend.repository.CourseRepository;
import io.pace.backend.repository.StudentAssessmentRepository;
import io.pace.backend.repository.UniversityRepository;
import io.pace.backend.service.course.CourseRecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class AssessmentService {

    @Autowired
    StudentAssessmentRepository studentAssessmentRepository;

    @Autowired
    UniversityRepository universityRepository;

    @Autowired
    CourseRepository courseRepository;

    public Map<String, Long> getAssessmentStats(Long universityId, Long courseId) {
        long total = studentAssessmentRepository.countByUniversity_UniversityId(universityId);
        long assessed = studentAssessmentRepository
                .countByUniversity_UniversityIdAndRecommendedCourse_CourseId(universityId, courseId);

        return Map.of(
                "assessed", assessed,
                "total", total
        );
    }

    public StudentAssessmentResponse saveStudentAssessment(StudentAssessmentRequest request) {
        University university = universityRepository.findById(Math.toIntExact(request.getUniversityId()))
                .orElseThrow(() -> new RuntimeException("University not found"));


        StudentAssessment studentAssessment = new StudentAssessment();
        studentAssessment.setEmail(request.getEmail());
        studentAssessment.setUserName(request.getUserName());
        studentAssessment.setUniversity(university);

        studentAssessmentRepository.save(studentAssessment);

        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String dateString = today.format(formatter);

        return new StudentAssessmentResponse(
                studentAssessment.getEmail(),
                studentAssessment.getUserName(),
                null,
                "Completed",
                dateString
        );
    }

    public void saveRecommendedCourse(Long studentAssessmentId, Long topCourseId) {
        StudentAssessment studentAssessment = studentAssessmentRepository.findById(studentAssessmentId)
                .orElseThrow(() -> new RuntimeException("StudentAssessment not found"));

        Course course = courseRepository.findById(topCourseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        studentAssessment.setRecommendedCourse(course);
        studentAssessmentRepository.save(studentAssessment);
    }


}
