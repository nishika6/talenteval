package com.talenteval.talenteval.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AddQuestionsRequest {

    @NotEmpty(message = "Question IDs are required")
    private List<Long> questionIds;
}
