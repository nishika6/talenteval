package com.talenteval.talenteval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private Long interviewerId;
    private String interviewerName;
    private Long candidateId;
    private String candidateName;
    private LocalDateTime date;
    private String status;
    private List<SessionQuestionResponse> questions;
}
