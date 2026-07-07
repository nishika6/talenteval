package com.talenteval.talenteval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SessionProgressResponse {

    private Long sessionId;
    private String interviewerName;
    private LocalDateTime date;
    private int communication;
    private int structure;
    private int content;
    private int confidence;
    private String comments;
}
