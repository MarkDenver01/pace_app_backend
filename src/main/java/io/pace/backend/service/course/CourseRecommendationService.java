package io.pace.backend.service.course;

import io.pace.backend.data.entity.Course;
import io.pace.backend.data.entity.Questions;
import io.pace.backend.domain.request.AnsweredQuestionRequest;
import io.pace.backend.domain.response.CourseMatchResponse;
import io.pace.backend.repository.CourseRepository;
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
public class CourseRecommendationService {

    @Autowired
    private final CourseRepository courseRepository;

    public List<CourseMatchResponse> getTopCourses(List<AnsweredQuestionRequest> answeredQuestions) {
        Map<Long, String> answerMap = answeredQuestions.stream()
                .collect(Collectors.toMap(AnsweredQuestionRequest::getQuestionId, AnsweredQuestionRequest::getAnswer));

        List<Course> activeCourses = courseRepository.findByStatusIgnoreCase("ACTIVE");

        List<CourseMatchResponse> scoredCourses = new ArrayList<>();

        for (Course course : activeCourses) {
            List<Questions> courseQuestions = course.getQuestions();
            long total = courseQuestions.size();
            long yesCount = courseQuestions.stream()
                    .filter(q -> "Yes".equalsIgnoreCase(answerMap.get(q.getQuestionId())))
                    .count();

            double percentage = total == 0 ? 0.0 : (yesCount * 100.0 / total);

            String message = getRecommendationMessage(percentage, course.getCourseName());

            scoredCourses.add(new CourseMatchResponse(
                    course.getCourseId(),
                    course.getCourseName(),
                    course.getCourseDescription(),
                    percentage,
                    message
            ));
        }

        return scoredCourses.stream()
                .sorted(Comparator.comparingDouble(CourseMatchResponse::getMatchPercentage).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    private String getRecommendationMessage(double percent, String courseName) {
        if (percent >= 80.0) {
            return "You show strong alignment in both interest and personality traits for a " + courseName +
                    ". You’re likely to thrive in the course and enjoy a career in related fields.";
        } else if (percent >= 50.0) {
            return "You have a moderate fit for " + courseName +
                    ". Consider exploring specific areas to see what excites you.";
        } else {
            return "This field may not fully match your current interests or personality traits for " + courseName +
                    ". However, if you're curious, try exploring more or take an intro class.";
        }
    }
}
