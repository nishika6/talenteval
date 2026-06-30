package com.talenteval.talenteval.controller;

import com.talenteval.talenteval.dto.ProgressResponse;
import com.talenteval.talenteval.repository.UserRepository;
import com.talenteval.talenteval.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<ProgressResponse> getMyProgress(Authentication auth) {
        Long candidateId = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
        return ResponseEntity.ok(progressService.getProgressForCandidate(candidateId));
    }

    @GetMapping("/candidate/{candidateId}")
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<ProgressResponse> getCandidateProgress(@PathVariable Long candidateId) {
        return ResponseEntity.ok(progressService.getProgressForCandidate(candidateId));
    }
}
