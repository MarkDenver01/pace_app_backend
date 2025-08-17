package io.pace.backend.service.questions;

import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.domain.model.entity.Questions;
import io.pace.backend.domain.model.entity.University;
import io.pace.backend.domain.model.request.QuestionRequest;
import io.pace.backend.domain.model.response.QuestionResponse;
import io.pace.backend.repository.CourseRepository;
import io.pace.backend.repository.QuestionRepository;
import io.pace.backend.repository.UniversityRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class QuestionService {
    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    UniversityRepository universityRepository;

    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(q -> new QuestionResponse(
                        q.getQuestionId(),
                        q.getQuestion(),
                        q.getCategory().name(),
                        q.getCourse().getCourseName(),
                        q.getCourse().getCourseDescription(),
                        q.getUniversity().getUniversityId(),
                        q.getCourse().getUniversity().getUniversityName()
                ))
                .collect(Collectors.toList());
    }

    public List<QuestionResponse> getQuestionsByCourse(Long courseId) {
        return questionRepository.findByCourse_CourseId(courseId).stream()
                .map(q -> new QuestionResponse(
                        q.getQuestionId(),
                        q.getQuestion(),
                        q.getCategory().name(),
                        q.getCourse().getCourseName(),
                        q.getCourse().getCourseDescription(),
                        q.getUniversity().getUniversityId(),
                        q.getCourse().getUniversity().getUniversityName()
                ))
                .collect(Collectors.toList());
    }

    public List<QuestionResponse> getQuestionsByUniversity(Long universityId) {
        return questionRepository.findByCourse_University_UniversityId(universityId).stream()
                .map(q -> new QuestionResponse(
                        q.getQuestionId(),
                        q.getQuestion(),
                        q.getCategory().name(),
                        q.getCourse().getCourseName(),
                        q.getCourse().getCourseDescription(),
                        q.getUniversity().getUniversityId(),
                        q.getCourse().getUniversity().getUniversityName()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Questions saveQuestion(QuestionRequest request) {
        Optional<Course> courseOpt = courseRepository.findById(request.getCourseId());
        if (courseOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid course ID");
        }

        University university = universityRepository.findById(Math.toIntExact(request.getUniversityId()))
                .orElseThrow(() -> new IllegalArgumentException("University not found with ID: " + request.getUniversityId()));


        Questions question = new Questions();
        question.setCourse(courseOpt.get());
        question.setCategory(request.getCategory());
        question.setQuestion(request.getQuestion());
        question.setUniversity(university);

        return questionRepository.save(question);
    }

}
