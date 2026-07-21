package com.talenteval.talenteval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionQuestionResponse {

    private Long id;
    private String title;
    private String role;
    private String topic;
    private String difficulty;
    private int questionOrder;
    private int timeLimit;
}
