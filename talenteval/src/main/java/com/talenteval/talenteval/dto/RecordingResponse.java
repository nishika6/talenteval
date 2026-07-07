package com.talenteval.talenteval.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecordingResponse {

    private Long questionId;
    private String url;
}
