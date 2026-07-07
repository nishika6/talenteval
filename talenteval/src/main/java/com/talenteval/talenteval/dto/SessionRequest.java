package com.talenteval.talenteval.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SessionRequest {

    @NotNull(message = "Candidate ID is required")
    private Long candidateId;

    private LocalDateTime scheduledAt;
}
