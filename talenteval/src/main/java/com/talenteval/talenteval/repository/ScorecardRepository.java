package com.talenteval.talenteval.repository;

import com.talenteval.talenteval.entity.Scorecard;
import com.talenteval.talenteval.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScorecardRepository extends JpaRepository<Scorecard, Long> {

    Optional<Scorecard> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    List<Scorecard> findByCandidateOrderBySessionDateAsc(User candidate);
}
