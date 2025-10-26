package io.pace.backend.service.career;

import io.pace.backend.domain.model.entity.Career;
import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.repository.CareerRepository;
import io.pace.backend.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CareerService {
    @Autowired
    CourseRepository courseRepository;

    @Autowired
    CareerRepository careerRepository;

    /**
     * Create a new Career under a specific Course.
     */
    public Career createCareer(Long courseId, String careerName) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with ID: " + courseId));

        Career career = new Career();
        career.setCareer(careerName);
        career.setCourse(course);

        return careerRepository.save(career);
    }

    /**
     * Get a Career by its ID.
     */
    public Career getCareerById(Long careerId) {
        return careerRepository.findByCareerId(careerId)
                .orElseThrow(() -> new EntityNotFoundException("Career not found with ID: " + careerId));
    }

    /**
     * Get a Career by its name and Course name.
     */
    public Career getCareerByNameAndCourseName(String careerName, String courseName) {
        return careerRepository.findByCareer_AndCourse_CourseName(careerName, courseName)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Career '%s' not found under Course '%s'", careerName, courseName)
                ));
    }

    /**
     * Get a Career by ID and Course name.
     */
    public Career getCareerByIdAndCourseName(Long careerId, String courseName) {
        return careerRepository.findByCareerId_AndCourse_CourseName(careerId, courseName)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Career with ID %d not found under Course '%s'", careerId, courseName)
                ));
    }

    /**
     * Get all Careers under a specific Course.
     */
    public List<Career> getCareersByCourseId(Long courseId) {
        return careerRepository.findByCourse_CourseId(courseId);
    }

    /**
     * Update a Career name.
     */
    public Career updateCareer(Long careerId, String newCareerName) {
        Career career = careerRepository.findByCareerId(careerId)
                .orElseThrow(() -> new EntityNotFoundException("Career not found with ID: " + careerId));

        career.setCareer(newCareerName);
        return careerRepository.save(career);
    }

    /**
     * Delete a Career by ID.
     */
    public void deleteCareer(Long careerId) {
        if (!careerRepository.existsById(careerId)) {
            throw new EntityNotFoundException("Career not found with ID: " + careerId);
        }
        careerRepository.deleteById(careerId);
    }
}
