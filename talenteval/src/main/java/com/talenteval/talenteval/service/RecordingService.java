package com.talenteval.talenteval.service;

import com.talenteval.talenteval.dto.RecordingResponse;
import com.talenteval.talenteval.entity.InterviewSession;
import com.talenteval.talenteval.entity.Question;
import com.talenteval.talenteval.entity.SessionRecording;
import com.talenteval.talenteval.repository.SessionRecordingRepository;
import com.talenteval.talenteval.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecordingService {

    private final SessionRecordingRepository recordingRepository;
    private final SessionRepository sessionRepository;
    private final RecordingStorageService storageService;

    @Transactional
    public RecordingResponse uploadRecording(Long sessionId, Long questionId, MultipartFile file, String callerEmail) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getCandidate().getEmail().equals(callerEmail)) {
            throw new IllegalArgumentException("You are not the candidate of this session");
        }

        Question question = session.getSessionQuestions().stream()
                .map(sq -> sq.getQuestion())
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Question is not part of this session"));

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded recording", e);
        }

        String storageKey = storageService.store(data, sessionId, questionId);

        SessionRecording recording = recordingRepository.findBySessionIdAndQuestionId(sessionId, questionId)
                .orElse(SessionRecording.builder().session(session).question(question).build());
        recording.setFilePath(storageKey);
        recording.setUploadedAt(LocalDateTime.now());
        recordingRepository.save(recording);

        return toResponse(recording);
    }

    public List<RecordingResponse> getRecordings(Long sessionId, String callerEmail) {
        InterviewSession session = getSessionForParticipant(sessionId, callerEmail);
        return recordingRepository.findBySessionId(session.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    public byte[] getAudio(Long sessionId, Long questionId, String callerEmail) {
        getSessionForParticipant(sessionId, callerEmail);

        SessionRecording recording = recordingRepository.findBySessionIdAndQuestionId(sessionId, questionId)
                .orElseThrow(() -> new IllegalArgumentException("Recording not found for this question"));

        return storageService.load(recording.getFilePath());
    }

    public boolean isFullyRecorded(InterviewSession session) {
        long questionCount = session.getSessionQuestions().size();
        long recordingCount = recordingRepository.countBySessionId(session.getId());
        return questionCount > 0 && recordingCount >= questionCount;
    }

    private InterviewSession getSessionForParticipant(Long sessionId, String callerEmail) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!session.getInterviewer().getEmail().equals(callerEmail)
                && !session.getCandidate().getEmail().equals(callerEmail)) {
            throw new IllegalArgumentException("You are not a participant of this session");
        }

        return session;
    }

    private RecordingResponse toResponse(SessionRecording recording) {
        Long sessionId = recording.getSession().getId();
        Long questionId = recording.getQuestion().getId();
        String url = "/sessions/" + sessionId + "/recordings/" + questionId + "/audio";
        return new RecordingResponse(questionId, url);
    }
}
