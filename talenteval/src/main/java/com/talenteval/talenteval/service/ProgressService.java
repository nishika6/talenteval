package com.talenteval.talenteval.service;

import com.talenteval.talenteval.dto.AverageScores;
import com.talenteval.talenteval.dto.ProgressResponse;
import com.talenteval.talenteval.dto.SessionProgressResponse;
import com.talenteval.talenteval.entity.Role;
import com.talenteval.talenteval.entity.Scorecard;
import com.talenteval.talenteval.entity.User;
import com.talenteval.talenteval.repository.ScorecardRepository;
import com.talenteval.talenteval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ScorecardRepository scorecardRepository;
    private final UserRepository userRepository;

    public ProgressResponse getProgressForCandidate(Long candidateId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Candidate not found"));

        if (candidate.getRole() != Role.CANDIDATE) {
            throw new IllegalArgumentException("User is not a candidate");
        }

        List<Scorecard> scorecards = scorecardRepository.findByCandidateOrderBySessionDateAsc(candidate);

        List<SessionProgressResponse> sessions = scorecards.stream()
                .map(sc -> new SessionProgressResponse(
                        sc.getSession().getId(),
                        sc.getSession().getInterviewer().getName(),
                        sc.getSession().getDate(),
                        sc.getCommunication(),
                        sc.getStructure(),
                        sc.getContent(),
                        sc.getConfidence(),
                        sc.getComments()
                ))
                .toList();

        AverageScores averages = computeAverages(scorecards);

        return new ProgressResponse(
                candidate.getId(),
                candidate.getName(),
                sessions.size(),
                averages,
                sessions
        );
    }

    private AverageScores computeAverages(List<Scorecard> scorecards) {
        if (scorecards.isEmpty()) {
            return new AverageScores(0, 0, 0, 0);
        }

        double communication = scorecards.stream().mapToInt(Scorecard::getCommunication).average().orElse(0);
        double structure = scorecards.stream().mapToInt(Scorecard::getStructure).average().orElse(0);
        double content = scorecards.stream().mapToInt(Scorecard::getContent).average().orElse(0);
        double confidence = scorecards.stream().mapToInt(Scorecard::getConfidence).average().orElse(0);

        return new AverageScores(
                round1(communication),
                round1(structure),
                round1(content),
                round1(confidence)
        );
    }

    private double round1(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
