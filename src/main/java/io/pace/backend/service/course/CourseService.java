package io.pace.backend.service.course;

import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.request.CourseRequest;
import io.pace.backend.domain.model.response.CourseResponse;
import io.pace.backend.repository.CourseRepository;
import io.pace.backend.repository.StudentAssessmentRepository;
import io.pace.backend.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    StudentAssessmentRepository studentAssessmentRepository;

    @Autowired
    UniversityRepository universityRepository;

    public List<Course> getCoursesByStatus(String status) {
        return courseRepository.findAllByStatus(status);
    }

    public List<Course> searchCourses(String courseName) {
        return courseRepository.findByCourseNameContainingIgnoreCase(courseName);
    }

    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        courseRepository.delete(course);
    }

    public List<CourseResponse> getAllCourses() {
        // Fetch all courses
        List<Course> courses = courseRepository.findAll();

        // Fetch counts from assessments
        List<Object[]> counts = studentAssessmentRepository.countStudentsPerCourseOverall();

        // Convert to map for easy lookup
        Map<String, Long> assessedCountMap = counts.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],   // courseDescription
                        row -> (Long) row[1]
                ));

        // Map results to CourseResponse
        return courses.stream()
                .map(c -> {
                    long assessed = assessedCountMap.getOrDefault(c.getCourseName(), 0L);
                    int max = 0; // if you plan to use this later
                    return new CourseResponse(
                            c.getCourseId(),
                            c.getCourseName(),
                            c.getCourseDescription(),
                            c.getStatus(),
                            (int) assessed,
                            max
                    );
                })
                .collect(Collectors.toList());
    }

    public CourseResponse saveCourse(CourseRequest request) {
        if (courseRepository.existsByCourseNameIgnoreCase(request.getCourseName())) {
            throw new IllegalArgumentException("Course with the same name already exists.");
        }

        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCourseDescription(request.getCourseDescription());
        course.setStatus(
                (request.getStatus() == null || request.getStatus().isBlank()) ? "Active" : request.getStatus()
        );

        Course saved = courseRepository.save(course);
        return new CourseResponse(
                saved.getCourseId(),
                saved.getCourseName(),
                saved.getCourseDescription(),
                saved.getStatus());
    }

    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        existingCourse.setCourseName(request.getCourseName());
        existingCourse.setCourseDescription(request.getCourseDescription());
        existingCourse.setStatus(
                (request.getStatus() == null || request.getStatus().isBlank()) ? existingCourse.getStatus() : request.getStatus()
        );

        Course updated = courseRepository.save(existingCourse);

        return new CourseResponse(
                updated.getCourseId(),
                updated.getCourseName(),
                updated.getCourseDescription(),
                updated.getStatus()
        );
    }

    public List<Course> getAllActiveCourses(String activeStatus) {
        return courseRepository.findAllByStatus(activeStatus);
    }

    public long getCourseCount( String status) {
        return courseRepository.countByStatus(status);
    }
}
