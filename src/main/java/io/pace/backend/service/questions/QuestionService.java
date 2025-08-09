package io.pace.backend.service.questions;

import io.pace.backend.domain.model.response.QuestionResponse;
import io.pace.backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class QuestionService {
    @Autowired
    private final QuestionRepository questionRepository;

    public List<QuestionResponse> getAllQuestions() {


        return questionRepository.findAll().stream()
                .map(q-> new QuestionResponse(
                        q.getQuestionId(),
                        q.getQuestion(),
                        q.getCategory().name(),
                        q.getCourse().getCourseName()
                ))
                .collect(Collectors.toList());
    }
}
