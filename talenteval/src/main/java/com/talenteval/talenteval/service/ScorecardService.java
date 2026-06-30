package com.talenteval.talenteval.service;

import com.talenteval.talenteval.dto.ScorecardRequest;
import com.talenteval.talenteval.dto.ScorecardResponse;
import com.talenteval.talenteval.entity.InterviewSession;
import com.talenteval.talenteval.entity.Scorecard;
import com.talenteval.talenteval.entity.SessionStatus;
import com.talenteval.talenteval.entity.User;
import com.talenteval.talenteval.repository.ScorecardRepository;
import com.talenteval.talenteval.repository.SessionRepository;
import com.talenteval.talenteval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScorecardService {

    private final ScorecardRepository scorecardRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    public ScorecardResponse submitScorecard(String interviewerEmail, ScorecardRequest request) {
        InterviewSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getInterviewer().getEmail().equals(interviewerEmail)) {
            throw new IllegalArgumentException("You are not the interviewer of this session");
        }

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot submit scorecard for an incomplete session");
        }

        if (scorecardRepository.existsBySessionId(session.getId())) {
            throw new IllegalArgumentException("A scorecard already exists for this session");
        }

        Scorecard scorecard = Scorecard.builder()
                .session(session)
                .candidate(session.getCandidate())
                .communication(request.getCommunication())
                .structure(request.getStructure())
                .content(request.getContent())
                .confidence(request.getConfidence())
                .comments(request.getComments())
                .build();

        scorecardRepository.save(scorecard);
        return toResponse(scorecard);
    }

    public ScorecardResponse getBySession(Long sessionId, String email) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getInterviewer().getEmail().equals(email)
                && !session.getCandidate().getEmail().equals(email)) {
            throw new IllegalArgumentException("You are not a participant of this session");
        }

        Scorecard scorecard = scorecardRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Scorecard not found for this session"));

        return toResponse(scorecard);
    }

    public List<ScorecardResponse> getByCandidate(Long candidateId, String requesterEmail) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isSelf = requester.getId().equals(candidateId);
        boolean isInterviewer = requester.getRole().name().equals("INTERVIEWER");

        if (!isSelf && !isInterviewer) {
            throw new IllegalArgumentException("You are not authorized to view this candidate's scorecards");
        }

        return scorecardRepository.findByCandidateOrderBySessionDateAsc(candidate)
                .stream().map(this::toResponse).toList();
    }

    private ScorecardResponse toResponse(Scorecard scorecard) {
        return new ScorecardResponse(
                scorecard.getId(),
                scorecard.getSession().getId(),
                scorecard.getCandidate().getId(),
                scorecard.getCandidate().getName(),
                scorecard.getCommunication(),
                scorecard.getStructure(),
                scorecard.getContent(),
                scorecard.getConfidence(),
                scorecard.getComments(),
                scorecard.getSession().getDate()
        );
    }
}
