package com.talenteval.talenteval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ScorecardResponse {

    private Long id;
    private Long sessionId;
    private Long candidateId;
    private String candidateName;
    private int communication;
    private int structure;
    private int content;
    private int confidence;
    private String comments;
    private LocalDateTime sessionDate;
}
