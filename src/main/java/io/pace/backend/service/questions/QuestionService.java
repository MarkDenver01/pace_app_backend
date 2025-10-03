package io.pace.backend.service.questions;

import io.pace.backend.domain.model.entity.Course;
import io.pace.backend.domain.model.entity.Questions;
import io.pace.backend.domain.model.request.QuestionRequest;
import io.pace.backend.domain.model.response.QuestionResponse;
import io.pace.backend.repository.CourseRepository;
import io.pace.backend.repository.QuestionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<QuestionResponse> getAllQuestions() {
        return new ArrayList<>(questionRepository.findAll().stream()
                // Collect to a Map with question text as the key to remove duplicates
                .collect(Collectors.toMap(
                        Questions::getQuestion, // key: question text
                        q -> new QuestionResponse(
                                q.getQuestionId(),
                                q.getQuestion(),
                                q.getCategory().name(),
                                q.getCourse().getCourseName(),
                                q.getCourse().getCourseDescription()
                        ),
                        (existing, replacement) -> existing // if duplicate, keep the first
                ))
                .values());
    }

    public List<QuestionResponse> getQuestionsByCourse(Long courseId) {
        return questionRepository.findByCourse_CourseId(courseId).stream()
                .map(q -> new QuestionResponse(
                        q.getQuestionId(),
                        q.getQuestion(),
                        q.getCategory().name(),
                        q.getCourse().getCourseName(),
                        q.getCourse().getCourseDescription()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public Questions saveQuestion(QuestionRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid course ID"));

        Questions question = new Questions();
        question.setCourse(course);
        question.setCategory(request.getCategory());
        question.setQuestion(request.getQuestion());

        return questionRepository.save(question);
    }

}
