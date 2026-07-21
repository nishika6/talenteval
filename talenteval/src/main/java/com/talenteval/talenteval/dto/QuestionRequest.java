package com.talenteval.talenteval.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class QuestionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "HR|UX|PM|FINANCE|ENGINEERING", message = "Role must be HR, UX, PM, FINANCE, or ENGINEERING")
    private String role;

    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Difficulty is required")
    @Pattern(regexp = "EASY|MEDIUM|HARD", message = "Difficulty must be EASY, MEDIUM, or HARD")
    private String difficulty;

    @Min(value = 10, message = "Time limit must be at least 10 seconds")
    @Max(value = 1800, message = "Time limit must be at most 1800 seconds")
    private Integer timeLimit;
}
