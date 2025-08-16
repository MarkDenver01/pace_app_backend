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

    public List<CourseResponse> getAvailableCourses(Long universityId, String status) {
        return courseRepository.findByUniversity_UniversityIdAndStatus(universityId, status).stream()
                .map(c -> new CourseResponse(
                        c.getCourseId(),
                        c.getCourseName(),
                        c.getCourseDescription(),
                        c.getStatus(),
                        c.getUniversity().getUniversityName(),
                        universityId
                ))
                .collect(Collectors.toList());
    }

    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(c -> {
                    int max = 0;
                    int assessed = 0;

                    // count all students in the university as the course
                    if (c.getUniversity() != null && c.getUniversity().getStudents() != null) {
                        max = c.getUniversity().getStudents().size();
                    }

                    // no assessment logic yet, so return to 0
                    assessed = 0;

                    return new CourseResponse(
                            c.getCourseId(),
                            c.getCourseName(),
                            c.getCourseDescription(),
                            c.getStatus(),
                            c.getUniversity().getUniversityId(),
                            c.getUniversity().getUniversityName(),
                            max,
                            assessed
                    );
                })
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
        return new CourseResponse(
                saved.getCourseId(),
                saved.getCourseName(),
                saved.getCourseDescription(),
                saved.getStatus(),
                university.getUniversityName(),
                university.getUniversityId());
    }

    public CourseResponse updateCourse(Long courseId, CourseRequest request) {
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));

        University university = universityRepository.findById(Math.toIntExact(request.getUniversityId()))
                .orElseThrow(() -> new IllegalArgumentException("University not found with ID: " + request.getUniversityId()));

        existingCourse.setCourseName(request.getCourseName());
        existingCourse.setCourseDescription(request.getCourseDescription());
        existingCourse.setUniversity(university);
        existingCourse.setStatus(
                (request.getStatus() == null || request.getStatus().isBlank()) ? existingCourse.getStatus() : request.getStatus()
        );

        Course updated = courseRepository.save(existingCourse);

        return new CourseResponse(
                updated.getCourseId(),
                updated.getCourseName(),
                updated.getCourseDescription(),
                updated.getStatus(),
                updated.getUniversity().getUniversityName(),
                updated.getUniversity().getUniversityId()
        );
    }


    public long getCourseCountByUniversity(Long universityId, String status) {
        return courseRepository.countByUniversity_UniversityIdAndStatus(universityId, status);
    }

    public List<Course> getActiveCoursesByUniversity(Long universityId) {
        return courseRepository.findByUniversity_UniversityIdAndStatusIgnoreCase(universityId, "Active");
    }

    public List<Course> getAllCoursesByUniversity(Long universityId) {
        return courseRepository.findByUniversity_UniversityId(universityId);
    }

    public void updateCourseStatus(Long courseId, String status) {
        Course existingCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
        existingCourse.setStatus(status);
        courseRepository.save(existingCourse);
    }
}
