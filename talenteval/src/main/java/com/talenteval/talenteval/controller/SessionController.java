package com.talenteval.talenteval.controller;

import com.talenteval.talenteval.dto.AddQuestionsRequest;
import com.talenteval.talenteval.dto.SessionRequest;
import com.talenteval.talenteval.dto.SessionResponse;
import com.talenteval.talenteval.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody SessionRequest request,
                                                         Authentication auth) {
        return ResponseEntity.ok(sessionService.createSession(auth.getName(), request));
    }

    @GetMapping
    public ResponseEntity<List<SessionResponse>> getSessions(Authentication auth) {
        return ResponseEntity.ok(sessionService.getSessionsForUser(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(sessionService.getSession(id, auth.getName()));
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<SessionResponse> addQuestions(@PathVariable Long id,
                                                        @Valid @RequestBody AddQuestionsRequest request,
                                                        Authentication auth) {
        return ResponseEntity.ok(sessionService.addQuestions(id, auth.getName(), request));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<SessionResponse> completeSession(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(sessionService.completeSession(id, auth.getName()));
    }
}
