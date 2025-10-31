package io.pace.backend.service.assessment;

import io.pace.backend.domain.model.entity.*;
import io.pace.backend.domain.model.request.StudentAssessmentRequest;
import io.pace.backend.domain.model.response.*;
import io.pace.backend.repository.StudentAssessmentRepository;
import io.pace.backend.repository.UniversityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        studentAssessment.setCreatedDateTime(LocalDateTime.now());
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
                    List<RecommendedCareer> careerList = rcReq.getCareers()
                            .stream()
                            .map(cReq -> {
                                RecommendedCareer career = new RecommendedCareer();
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
                                .map(c -> new RecommendedCareerResponse(c.getCareerId(), c.getCareer()))
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
                savedStudent.getCreatedDateTime().toString(),
                savedStudent.getAssessmentStatus(),
                recommendedCourseResponses
        );
    }

    public Map<String, Long> getAssessmentStats(Long universityId, Long courseId) {
        long total = studentAssessmentRepository.countByUniversity_UniversityId(universityId);
        long assessed = studentAssessmentRepository
                .countByUniversity_UniversityIdAndRecommendedCourses_CourseId(universityId, courseId);

        return Map.of(
                "assessed", assessed,
                "total", total
        );
    }

    public long getTotalAssessments(Long universityId) {
        return studentAssessmentRepository.countByUniversity_UniversityId(universityId);
    }

    public long getTotalSameSchool(Long universityId) {
        return studentAssessmentRepository
                .countByUniversity_UniversityIdAndEnrollmentStatusIgnoreCase(universityId, "Same School");
    }

    public long getTotalOtherSchool(Long universityId) {
        return studentAssessmentRepository
                .countByUniversity_UniversityIdAndEnrollmentStatusIgnoreCase(universityId, "Other School");
    }

    public long getTotalNewSchool(Long universityId) {
        return studentAssessmentRepository
                .countByUniversity_UniversityIdAndEnrollmentStatusIgnoreCase(universityId, "New School");
    }


    public StudentAssessmentResponse getStudentAssessment(Long universityId, String email) {
        return studentAssessmentRepository
                .findByUniversity_UniversityIdAndEmail(universityId, email)
                .map(student -> new StudentAssessmentResponse(
                        student.getStudentId(),
                        student.getUserName(),
                        student.getEmail(),
                        student.getEnrollmentStatus(),
                        student.getEnrolledUniversity(),
                        student.getUniversity() != null ? student.getUniversity().getUniversityId() : null,
                        student.getCreatedDateTime().toString(),
                        student.getAssessmentStatus(),
                        student.getRecommendedCourses().stream().map(rc -> new RecommendedCourseResponse(
                                rc.getCourseId(),
                                rc.getCourseDescription(),
                                rc.getAssessmentResult(),
                                rc.getResultDescription(),
                                rc.getStudentAssessment() != null ? rc.getStudentAssessment().getStudentId() : null,
                                rc.getCareers().stream().map(c -> new RecommendedCareerResponse(
                                        c.getCareerId(),
                                        c.getCareer()
                                )).toList()
                        )).toList()
                ))
                .orElseThrow(() -> new EntityNotFoundException("Student assessment not found"));
    }

    public List<StudentAssessmentResponse> getAllAssessmentsByUniversity(Long universityId) {
        List<StudentAssessment> assessments = studentAssessmentRepository.findByUniversity_UniversityId(universityId);

        if (assessments.isEmpty()) {
            throw new EntityNotFoundException("No student assessments found for university ID: " + universityId);
        }

        return assessments.stream()
                .map(student -> new StudentAssessmentResponse(
                        student.getStudentId(),
                        student.getUserName(),
                        student.getEmail(),
                        student.getEnrollmentStatus(),
                        student.getEnrolledUniversity(),
                        student.getUniversity() != null ? student.getUniversity().getUniversityId() : null,
                        student.getCreatedDateTime().toString(),
                        student.getAssessmentStatus(),
                        student.getRecommendedCourses().stream()
                                .map(rc -> new RecommendedCourseResponse(
                                        rc.getCourseId(),
                                        rc.getCourseDescription(),
                                        rc.getAssessmentResult(),
                                        rc.getResultDescription(),
                                        rc.getStudentAssessment() != null ? rc.getStudentAssessment().getStudentId() : null,
                                        rc.getCareers().stream()
                                                .map(c -> new RecommendedCareerResponse(
                                                        c.getCareerId(),
                                                        c.getCareer()
                                                ))
                                                .toList()
                                ))
                                .toList()
                ))
                .toList();
    }


    public void deleteStudentAssessment(String email) {
        StudentAssessment student = studentAssessmentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("StudentAssessment not found"));

        studentAssessmentRepository.delete(student);

    }

    public List<TopCourseResponse> getTopCourses(Long universityId) {
        LocalDate today = LocalDate.now();
        LocalDate startDateLocal = today.minusMonths(5);

        LocalDateTime startDateTime = startDateLocal.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = today.atTime(LocalTime.MAX);      // 23:59:59.999999999

        List<Object[]> results = studentAssessmentRepository.findTopCoursesByDateRange(
                universityId, startDateTime, endDateTime);

        return results.stream()
                .map(r -> new TopCourseResponse((String) r[0], ((Long) r[1]).intValue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<TopCourseResponse> getTopCoursesByUniversityAutoDate(Long universityId) {
        // Fetch date range dynamically from DB
        LocalDateTime fromDate = studentAssessmentRepository.findEarliestDate(universityId);
        LocalDateTime toDate = studentAssessmentRepository.findLatestDate(universityId);

        if (fromDate == null || toDate == null) {
            return List.of(); // No data
        }

        List<Object[]> results = studentAssessmentRepository.findTopCoursesByUniversityAndDateRange(
                universityId, fromDate, toDate
        );

        return results.stream()
                .map(obj -> new TopCourseResponse((String) obj[0], ((Number) obj[1]).longValue()))
                .limit(5)
                .collect(Collectors.toList());
    }

    public List<TopCompetitorResponse> getTop3Competitors(Long universityId) {
        List<Object[]> results = studentAssessmentRepository.findTop3CompetitorUniversitiesByUniversityId(universityId);

        return results.stream()
                .map(r -> new TopCompetitorResponse(
                        (String) r[0],                  // competitorName
                        ((Number) r[1]).intValue()      // totalCount
                ))
                .collect(Collectors.toList());
    }

    public List<DailyAssessmentCountResponse> getDailyAssessmentCountForUniversity(Long universityId) {

        List<Object[]> minMaxList = studentAssessmentRepository.findMinAndMaxCreatedDateByUniversity(universityId);

        if (minMaxList == null || minMaxList.isEmpty() || minMaxList.get(0)[0] == null || minMaxList.get(0)[1] == null) {
            return List.of(); // No data
        }

        Object[] minMaxDates = minMaxList.get(0);

        LocalDateTime start = (LocalDateTime) minMaxDates[0];
        LocalDateTime end = (LocalDateTime) minMaxDates[1];

        List<Object[]> results = studentAssessmentRepository.countAssessmentsByDateForUniversity(start, end, universityId);

        return results.stream()
                .map(r -> new DailyAssessmentCountResponse(
                        r[0].toString(),          // date
                        ((Number) r[1]).intValue()
                ))
                .collect(Collectors.toList());
    }

    public List<DailySameSchoolCountResponse> getDailySameSchoolCount(Long universityId) {

        // Get min and max dates
        List<Object[]> minMaxList = studentAssessmentRepository.findMinAndMaxCreatedDateByUniversity(universityId);

        if(minMaxList == null || minMaxList.isEmpty() || minMaxList.get(0)[0] == null || minMaxList.get(0)[1] == null) {
            return List.of(); // No data
        }

        Object[] minMaxDates = minMaxList.get(0);
        LocalDateTime start = (LocalDateTime) minMaxDates[0];
        LocalDateTime end = (LocalDateTime) minMaxDates[1];

        List<Object[]> results = studentAssessmentRepository.countSameSchoolStudentsByDate(start, end, universityId);

        return results.stream()
                .map(r -> new DailySameSchoolCountResponse(
                        r[0].toString(),                // date (yyyy-MM-dd)
                        ((Number) r[1]).intValue()     // count
                ))
                .collect(Collectors.toList());
    }

    public List<DailyOtherSchoolCountResponse> getDailyOtherSchoolCount(Long universityId) {

        // Get min and max dates
        List<Object[]> minMaxList = studentAssessmentRepository.findMinAndMaxCreatedDateByUniversity(universityId);

        if(minMaxList == null || minMaxList.isEmpty() || minMaxList.get(0)[0] == null || minMaxList.get(0)[1] == null) {
            return List.of(); // No data
        }

        Object[] minMaxDates = minMaxList.get(0);
        LocalDateTime start = (LocalDateTime) minMaxDates[0];
        LocalDateTime end = (LocalDateTime) minMaxDates[1];

        List<Object[]> results = studentAssessmentRepository.countOtherSchoolStudentsByDate(start, end, universityId);

        return results.stream()
                .map(r -> new DailyOtherSchoolCountResponse(
                        r[0].toString(),             // date (yyyy-MM-dd)
                        ((Number) r[1]).intValue()  // count
                ))
                .collect(Collectors.toList());
    }

    public List<DailyNewSchoolCountResponse> getDailyNewSchoolCount(Long universityId) {

        // Get min and max dates
        List<Object[]> minMaxList = studentAssessmentRepository.findMinAndMaxCreatedDateByUniversity(universityId);

        if(minMaxList == null || minMaxList.isEmpty() || minMaxList.get(0)[0] == null || minMaxList.get(0)[1] == null) {
            return List.of(); // No data
        }

        Object[] minMaxDates = minMaxList.get(0);
        LocalDateTime start = (LocalDateTime) minMaxDates[0];
        LocalDateTime end = (LocalDateTime) minMaxDates[1];

        List<Object[]> results = studentAssessmentRepository.countNewSchoolStudentsByDate(start, end, universityId);

        return results.stream()
                .map(r -> new DailyNewSchoolCountResponse(
                        r[0].toString(),             // date (yyyy-MM-dd)
                        ((Number) r[1]).intValue()  // count
                ))
                .collect(Collectors.toList());
    }

    public List<CourseCountResponse> getCourseCountsByUniversity(Long universityId) {

        List<Object[]> results = studentAssessmentRepository.countStudentsPerCourse(universityId);

        return results.stream()
                .map(r -> new CourseCountResponse(
                        (String) r[0],           // courseDescription
                        ((Number) r[1]).intValue() // total count
                ))
                .collect(Collectors.toList());
    }

    public List<CompetitorUniversityCountResponse> getCompetitorCounts(Long universityId) {

        // Get min & max date for competitor students
        Object[] minMax = studentAssessmentRepository.findMinAndMaxCreatedDateForCompetitors(universityId);

        if (minMax == null || minMax[0] == null || minMax[1] == null) {
            return List.of(); // no data
        }

        LocalDateTime start = (LocalDateTime) minMax[0];
        LocalDateTime end = (LocalDateTime) minMax[1];

        List<Object[]> results = studentAssessmentRepository.findCompetitorCountsByDate(start, end, universityId);

        return results.stream()
                .map(r -> new CompetitorUniversityCountResponse(
                        r[0].toString(),
                        r[1].toString()
                ))
                .collect(Collectors.toList());
    }


}
