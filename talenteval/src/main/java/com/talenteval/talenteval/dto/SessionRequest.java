package com.talenteval.talenteval.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SessionRequest {

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;
}
