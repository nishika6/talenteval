package com.talenteval.talenteval.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void notifyCandidate(String candidateEmail, String candidateName,
                                String interviewerName, LocalDateTime scheduledAt) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(candidateEmail);
        msg.setSubject("You have been assigned a mock interview on TalentEval");

        StringBuilder body = new StringBuilder();
        body.append("Hi ").append(candidateName).append(",\n\n");
        body.append("You have been assigned a mock interview session by ").append(interviewerName).append(".\n");
        if (scheduledAt != null) {
            String formatted = scheduledAt.format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a"));
            body.append("Scheduled for: ").append(formatted).append("\n");
        }
        body.append("\nOpen TalentEval: ").append(frontendUrl).append("/login\n\n");
        body.append("Best,\nTalentEval Team");

        msg.setText(body.toString());
        mailSender.send(msg);
    }

    @Async
    public void notifyInterviewer(String interviewerEmail, String interviewerName, String candidateName) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(interviewerEmail);
        msg.setSubject(candidateName + " has completed their mock interview");

        String body = "Hi " + interviewerName + ",\n\n"
                + candidateName + " has completed their mock interview session.\n\n"
                + "Please log in to TalentEval to review and fill in the scorecard.\n\n"
                + "Open TalentEval: " + frontendUrl + "/login\n\n"
                + "Best,\nTalentEval Team";

        msg.setText(body);
        mailSender.send(msg);
    }

    @Async
    public void sendPasswordResetEmail(String email, String name, String token) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Reset your TalentEval password");

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        String body = "Hi " + name + ",\n\n"
                + "We received a request to reset your TalentEval password.\n"
                + "Click the link below to set a new password (expires in 30 minutes):\n\n"
                + resetLink + "\n\n"
                + "If you didn't request this, you can safely ignore this email.\n\n"
                + "Best,\nTalentEval Team";

        msg.setText(body);
        mailSender.send(msg);
    }
}
