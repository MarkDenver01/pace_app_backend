package io.pace.backend.service.course;

import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.entity.UniversityCourse;
import io.pace.backend.domain.model.response.CourseResponse;
import io.pace.backend.repository.CourseRepository;
import io.pace.backend.repository.UniversityCourseRepository;
import io.pace.backend.repository.UniversityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UniversityCourseService {
    @Autowired
    UniversityCourseRepository universityCourseRepository;

    @Autowired
    UniversityRepository universityRepository;

    @Autowired
    CourseRepository courseRepository;

    public List<UniversityCourse> getCoursesForUniversity(Long universityId) {
        University university = universityRepository.findById(Math.toIntExact(universityId))
                .orElseThrow(() -> new RuntimeException("University not found"));
        return universityCourseRepository.findByUniversity(university);
    }

    public UniversityCourse activateCourse(Long universityId, Long courseId) {
        University university = universityRepository.findById(Math.toIntExact(universityId))
                .orElseThrow(() -> new RuntimeException("University not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // If exists, update status; if not, create new
        UniversityCourse uc = universityCourseRepository.findByUniversityAndCourse(university, course)
                .orElseGet(() -> {
                    UniversityCourse newUC = new UniversityCourse();
                    newUC.setUniversity(university);
                    newUC.setCourse(course);
                    return newUC;
                });

        uc.setStatus("Active"); // always update status
        return universityCourseRepository.save(uc);
    }

    public UniversityCourse deactivateCourse(Long universityId, Long courseId) {
        University university = universityRepository.findById(Math.toIntExact(universityId))
                .orElseThrow(() -> new RuntimeException("University not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        UniversityCourse uc = universityCourseRepository.findByUniversityAndCourse(university, course)
                .orElseThrow(() -> new RuntimeException("UniversityCourse not found"));

        uc.setStatus("Inactive"); // update existing
        return universityCourseRepository.save(uc);
    }

    public long getActiveCourseCountByUniversity(Long universityId, String status) {
        return universityCourseRepository.countByUniversity_UniversityIdAndStatus(universityId, status);
    }

    public UniversityCourse assignCourse(Long courseId, Long universityId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        University university = universityRepository.findById(Math.toIntExact(universityId))
                .orElseThrow(() -> new RuntimeException("University not found"));

        // prevent duplicates
        universityCourseRepository.findByUniversityAndCourse(university, course)
                .ifPresent(uc -> {
                    throw new RuntimeException("Course already assigned to this university");
                });

        UniversityCourse universityCourse = new UniversityCourse();
        universityCourse.setCourse(course);
        universityCourse.setUniversity(university);
        universityCourse.setStatus("Active"); // default

        return universityCourseRepository.save(universityCourse);
    }

    public List<CourseResponse> getActiveCoursesByUniversity(Long universityId) {
        return universityCourseRepository.findByUniversity_UniversityIdAndStatus(universityId, "Active")
                .stream()
                .map(uc -> new CourseResponse(
                        uc.getCourse().getCourseId(),
                        uc.getCourse().getCourseName(),
                        uc.getCourse().getCourseDescription(),
                        uc.getStatus()
                ))
                .toList();
    }
}
