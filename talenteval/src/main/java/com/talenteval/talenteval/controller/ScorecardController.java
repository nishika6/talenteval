package com.talenteval.talenteval.controller;

import com.talenteval.talenteval.dto.ScorecardRequest;
import com.talenteval.talenteval.dto.ScorecardResponse;
import com.talenteval.talenteval.service.ScorecardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scorecards")
@RequiredArgsConstructor
public class ScorecardController {

    private final ScorecardService scorecardService;

    @PostMapping
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<ScorecardResponse> submitScorecard(@Valid @RequestBody ScorecardRequest request,
                                                              Authentication auth) {
        return ResponseEntity.ok(scorecardService.submitScorecard(auth.getName(), request));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ScorecardResponse> getBySession(@PathVariable Long sessionId, Authentication auth) {
        return ResponseEntity.ok(scorecardService.getBySession(sessionId, auth.getName()));
    }

    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<List<ScorecardResponse>> getByCandidate(@PathVariable Long candidateId,
                                                                   Authentication auth) {
        return ResponseEntity.ok(scorecardService.getByCandidate(candidateId, auth.getName()));
    }
}
