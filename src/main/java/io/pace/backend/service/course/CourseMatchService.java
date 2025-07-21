package io.pace.backend.service.course;

import io.pace.backend.data.entity.Course;
import io.pace.backend.data.entity.Questions;
import io.pace.backend.domain.response.CourseMatchResponse;
import io.pace.backend.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseMatchService {

    @Autowired
    private final QuestionRepository questionRepository;

    public List<CourseMatchResponse> getCourseMatchResults(List<Long> yesAnsweredQuestionIds) {
        List<Questions> allQuestions = questionRepository.findAll();

        // Group questions by course
        Map<Course, List<Questions>> courseQuestionMap = allQuestions.stream()
                .collect(Collectors.groupingBy(Questions::getCourse));

        List<CourseMatchResponse> responses = new ArrayList<>();

        for (Map.Entry<Course, List<Questions>> entry : courseQuestionMap.entrySet()) {
            Course course = entry.getKey();
            List<Questions> courseQuestions = entry.getValue();
            long totalQuestions = courseQuestions.size();
            long matchedYes = courseQuestions.stream()
                    .filter(q -> yesAnsweredQuestionIds.contains(q.getQuestionId()))
                    .count();

            int percentage = (int) ((double) matchedYes / totalQuestions * 100);
            String message;

            if (percentage >= 80) {
                message = "You show strong alignment in both interest and personality traits for " + course.getCourseName();
            } else if (percentage >= 50) {
                message = "You have a moderate fit for " + course.getCourseName();
            } else {
                message = "This field may not fully match your current interests.";
            }

            responses.add(new CourseMatchResponse(
                    course.getCourseName(),
                    course.getCourseDescription(),
                    percentage,
                    message
            ));
        }

        // Sort by match % and limit to top 3
        return responses.stream()
                .sorted(Comparator.comparingInt(CourseMatchResponse::getMatchPercentage).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }
}
