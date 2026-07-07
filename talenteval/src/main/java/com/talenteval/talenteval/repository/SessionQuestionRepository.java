package com.talenteval.talenteval.repository;

import com.talenteval.talenteval.entity.SessionQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionQuestionRepository extends JpaRepository<SessionQuestion, Long> {
}
