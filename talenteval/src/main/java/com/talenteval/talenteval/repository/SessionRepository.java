package com.talenteval.talenteval.repository;

import com.talenteval.talenteval.entity.InterviewSession;
import com.talenteval.talenteval.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionRepository extends JpaRepository<InterviewSession, Long> {

    List<InterviewSession> findByInterviewerOrderByDateDesc(User interviewer);

    List<InterviewSession> findByCandidateOrderByDateDesc(User candidate);
}
