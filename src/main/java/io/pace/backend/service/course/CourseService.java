package io.pace.backend.service.course;

import io.pace.backend.data.entity.Course;
import io.pace.backend.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    @Autowired
    CourseRepository courseRepository;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByStatus(String status) {
        return courseRepository.findAllByStatus(status);
    }

    public List<Course> searchCourses(String courseName) {
        return courseRepository.findByCourseNameContainingIgnoreCase(courseName);
    }

    public void addCourse(Course course) {
        courseRepository.save(course);
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
}
