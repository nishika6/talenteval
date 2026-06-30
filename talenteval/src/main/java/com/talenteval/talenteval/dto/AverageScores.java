package com.talenteval.talenteval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AverageScores {

    private double communication;
    private double structure;
    private double content;
    private double confidence;
}
