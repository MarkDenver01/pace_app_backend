package io.pace.backend.service.assessment;

import io.pace.backend.domain.model.entity.*;
import io.pace.backend.domain.model.request.StudentAssessmentRequest;
import io.pace.backend.domain.model.response.*;
import io.pace.backend.repository.StudentAssessmentRepository;
import io.pace.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AssessmentService {

    @Autowired
    StudentAssessmentRepository studentAssessmentRepository;

    @Autowired
    UniversityRepository universityRepository;

    public StudentAssessmentResponse saveStudentAssessment(StudentAssessmentRequest request) {
        boolean alreadyTaken = studentAssessmentRepository.existsByEmail(request.getEmail());
        if (alreadyTaken) {
            throw new RuntimeException("This student has already completed the assessment.");
        }

        // Fetch University
        University university = universityRepository.findById(Math.toIntExact(request.getUniversityId()))
                .orElseThrow(() -> new RuntimeException("University not found"));

        // Create StudentAssessment entity
        StudentAssessment studentAssessment = new StudentAssessment();
        studentAssessment.setUserName(request.getUserName());
        studentAssessment.setEmail(request.getEmail());
        studentAssessment.setEnrollmentStatus(request.getEnrollmentStatus());
        studentAssessment.setEnrolledUniversity(request.getEnrolledUniversity());
        studentAssessment.setCreatedDateTime(LocalDateTime.now().toString());
        studentAssessment.setUniversity(university);
        studentAssessment.setAssessmentStatus(request.getAssessmentStatus());

        // Map RecommendedCourses
        List<RecommendedCourses> recommendedCoursesList = request.getRecommendedCourseRequests()
                .stream()
                .map(rcReq -> {
                    RecommendedCourses recommendedCourses = new RecommendedCourses();
                    recommendedCourses.setCourseDescription(rcReq.getCourseDescription());
                    recommendedCourses.setAssessmentResult(rcReq.getAssessmentResult());
                    recommendedCourses.setResultDescription(rcReq.getResultDescription());
                    recommendedCourses.setStudentAssessment(studentAssessment);

                    // Map Careers
                    List<Career> careerList = rcReq.getCareers()
                            .stream()
                            .map(cReq -> {
                                Career career = new Career();
                                career.setCareer(cReq.getCareerName());
                                career.setRecommendedCourses(recommendedCourses);
                                return career;
                            }).toList();

                    recommendedCourses.setCareers(careerList);
                    return recommendedCourses;
                }).toList();

        studentAssessment.setRecommendedCourses(recommendedCoursesList);

        // Save entity (cascades RecommendedCourses & Careers)
        StudentAssessment savedStudent = studentAssessmentRepository.save(studentAssessment);

        // Map response
        List<RecommendedCourseResponse> recommendedCourseResponses = savedStudent.getRecommendedCourses()
                .stream()
                .map(rc -> new RecommendedCourseResponse(
                        rc.getCourseId(),
                        rc.getCourseDescription(),
                        rc.getAssessmentResult(),
                        rc.getResultDescription(),
                        savedStudent.getStudentId(),
                        rc.getCareers()
                                .stream()
                                .map(c -> new CareerResponse(c.getCareerId(), c.getCareer(), rc.getCourseId()))
                                .toList()
                ))
                .toList();

        return new StudentAssessmentResponse(
                savedStudent.getStudentId(),
                savedStudent.getUserName(),
                savedStudent.getEmail(),
                savedStudent.getEnrollmentStatus(),
                savedStudent.getEnrolledUniversity(),
                savedStudent.getUniversity().getUniversityId(),
                savedStudent.getCreatedDateTime(),
                savedStudent.getAssessmentStatus(),
                recommendedCourseResponses
        );
    }
}
