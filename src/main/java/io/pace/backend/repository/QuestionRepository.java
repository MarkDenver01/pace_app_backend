package io.pace.backend.repository;

import io.pace.backend.data.entity.Questions;
import io.pace.backend.data.state.QuestionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Questions, Long> {


}
