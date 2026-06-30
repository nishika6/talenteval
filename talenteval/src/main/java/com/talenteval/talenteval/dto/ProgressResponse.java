package com.talenteval.talenteval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProgressResponse {

    private Long candidateId;
    private String candidateName;
    private int totalSessions;
    private AverageScores averageScores;
    private List<SessionProgressResponse> sessions;
}
