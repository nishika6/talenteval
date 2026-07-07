package com.talenteval.talenteval.controller;

import com.talenteval.talenteval.dto.UserResponse;
import com.talenteval.talenteval.entity.Role;
import com.talenteval.talenteval.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/candidates")
    @PreAuthorize("hasRole('INTERVIEWER')")
    public ResponseEntity<List<UserResponse>> getCandidates() {
        List<UserResponse> candidates = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.CANDIDATE)
                .map(u -> new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name()))
                .toList();
        return ResponseEntity.ok(candidates);
    }
}
