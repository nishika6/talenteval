package com.talenteval.talenteval.repository;

import com.talenteval.talenteval.entity.InterviewRole;
import com.talenteval.talenteval.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByRole(InterviewRole role);

    List<Question> findByRoleAndTopicContainingIgnoreCase(InterviewRole role, String topic);

    List<Question> findByTopicContainingIgnoreCase(String topic);
}
