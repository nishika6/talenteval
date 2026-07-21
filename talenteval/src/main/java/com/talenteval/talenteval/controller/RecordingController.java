package com.talenteval.talenteval.controller;

import com.talenteval.talenteval.dto.RecordingResponse;
import com.talenteval.talenteval.service.RecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/sessions/{sessionId}/recordings")
@RequiredArgsConstructor
public class RecordingController {

    private final RecordingService recordingService;

    @PostMapping
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<RecordingResponse> uploadRecording(@PathVariable Long sessionId,
                                                              @RequestParam Long questionId,
                                                              @RequestParam MultipartFile file,
                                                              Authentication auth) {
        return ResponseEntity.ok(recordingService.uploadRecording(sessionId, questionId, file, auth.getName()));
    }

    @GetMapping
    public ResponseEntity<List<RecordingResponse>> getRecordings(@PathVariable Long sessionId, Authentication auth) {
        return ResponseEntity.ok(recordingService.getRecordings(sessionId, auth.getName()));
    }

    @GetMapping("/{questionId}/video")
    public ResponseEntity<byte[]> getVideo(@PathVariable Long sessionId,
                                            @PathVariable Long questionId,
                                            Authentication auth) {
        byte[] video = recordingService.getVideo(sessionId, questionId, auth.getName());
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("video/webm"))
                .body(video);
    }
}
