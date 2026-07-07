package com.talenteval.talenteval.repository;

import com.talenteval.talenteval.entity.SessionRecording;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRecordingRepository extends JpaRepository<SessionRecording, Long> {

    List<SessionRecording> findBySessionId(Long sessionId);

    Optional<SessionRecording> findBySessionIdAndQuestionId(Long sessionId, Long questionId);

    long countBySessionId(Long sessionId);
}
