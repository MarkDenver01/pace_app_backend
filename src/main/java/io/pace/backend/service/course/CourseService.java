package io.pace.backend.service.course;

import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.request.CourseRequest;
import io.pace.backend.domain.model.response.CourseResponse;
import io.pace.backend.repository.CourseRepository;
import io.pace.backend.repository.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    @Autowired
    CourseRepository courseRepository;

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

    public void updateCourse(Long courseId,
                             String courseName,
                             String courseDescription,
                             String status) {
        try {
            Course updatedCourse = courseRepository.findById(courseId)
                    .orElseThrow(() ->
                            new RuntimeException("not found"));
            updatedCourse.setCourseName(courseName);
            updatedCourse.setCourseDescription(courseDescription);
            updatedCourse.setStatus(status);
            courseRepository.save(updatedCourse);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<CourseResponse> getAvailableCourses(Long universityId, String status) {
        return courseRepository.findByUniversity_UniversityIdAndStatus(universityId, status).stream()
                .map(c -> new CourseResponse(
                        c.getCourseName(),
                        c.getCourseDescription(),
                        c.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(c -> new CourseResponse(
                        c.getCourseName(),
                        c.getCourseDescription(),
                        c.getStatus()
                ))
                .collect(Collectors.toList());
    }

    public CourseResponse saveCourse(CourseRequest request) {
        University university = universityRepository.findById(Math.toIntExact(request.getUniversityId()))
                .orElseThrow(() -> new IllegalArgumentException("University not found with ID: " + request.getUniversityId()));

        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setCourseDescription(request.getCourseDescription());
        course.setUniversity(university);
        course.setStatus(
                (request.getStatus() == null || request.getStatus().isBlank()) ? "Active" : request.getStatus()
        );

        Course saved = courseRepository.save(course);
        return new CourseResponse(saved.getCourseName(), saved.getCourseDescription(), saved.getStatus());
    }

}
