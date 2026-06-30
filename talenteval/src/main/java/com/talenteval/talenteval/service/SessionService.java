package com.talenteval.talenteval.service;

import com.talenteval.talenteval.dto.*;
import com.talenteval.talenteval.entity.*;
import com.talenteval.talenteval.repository.QuestionRepository;
import com.talenteval.talenteval.repository.SessionRepository;
import com.talenteval.talenteval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public SessionResponse createSession(String interviewerEmail, SessionRequest request) {
        User interviewer = userRepository.findByEmail(interviewerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Interviewer not found"));

        User candidate = userRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        if (candidate.getRole() != Role.CANDIDATE) {
            throw new IllegalArgumentException("Selected user is not a candidate");
        }

        InterviewSession session = InterviewSession.builder()
                .interviewer(interviewer)
                .candidate(candidate)
                .date(LocalDateTime.now())
                .status(SessionStatus.IN_PROGRESS)
                .build();

        sessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public SessionResponse addQuestions(Long sessionId, String interviewerEmail, AddQuestionsRequest request) {
        InterviewSession session = getSessionForInterviewer(sessionId, interviewerEmail);

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Session is already completed");
        }

        session.getSessionQuestions().clear();

        int order = 1;
        for (Long questionId : request.getQuestionIds()) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found: " + questionId));

            SessionQuestion sq = SessionQuestion.builder()
                    .session(session)
                    .question(question)
                    .questionOrder(order++)
                    .build();

            session.getSessionQuestions().add(sq);
        }

        sessionRepository.save(session);
        return toResponse(session);
    }

    @Transactional
    public SessionResponse completeSession(Long sessionId, String interviewerEmail) {
        InterviewSession session = getSessionForInterviewer(sessionId, interviewerEmail);

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalArgumentException("Session is already completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);
        return toResponse(session);
    }

    public List<SessionResponse> getSessionsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<InterviewSession> sessions;
        if (user.getRole() == Role.INTERVIEWER) {
            sessions = sessionRepository.findByInterviewerOrderByDateDesc(user);
        } else {
            sessions = sessionRepository.findByCandidateOrderByDateDesc(user);
        }

        return sessions.stream().map(this::toResponse).toList();
    }

    public SessionResponse getSession(Long sessionId, String email) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getInterviewer().getEmail().equals(email)
                && !session.getCandidate().getEmail().equals(email)) {
            throw new IllegalArgumentException("You are not a participant of this session");
        }

        return toResponse(session);
    }

    private InterviewSession getSessionForInterviewer(Long sessionId, String interviewerEmail) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getInterviewer().getEmail().equals(interviewerEmail)) {
            throw new IllegalArgumentException("You are not the interviewer of this session");
        }

        return session;
    }

    private SessionResponse toResponse(InterviewSession session) {
        List<SessionQuestionResponse> questions = session.getSessionQuestions().stream()
                .map(sq -> new SessionQuestionResponse(
                        sq.getQuestion().getId(),
                        sq.getQuestion().getTitle(),
                        sq.getQuestion().getRole().name(),
                        sq.getQuestion().getTopic(),
                        sq.getQuestion().getDifficulty().name(),
                        sq.getQuestionOrder()
                ))
                .toList();

        return new SessionResponse(
                session.getId(),
                session.getInterviewer().getId(),
                session.getInterviewer().getName(),
                session.getCandidate().getId(),
                session.getCandidate().getName(),
                session.getDate(),
                session.getStatus().name(),
                questions
        );
    }
}
