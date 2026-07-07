package com.talenteval.talenteval.config;

import com.talenteval.talenteval.entity.Difficulty;
import com.talenteval.talenteval.entity.InterviewRole;
import com.talenteval.talenteval.entity.Question;
import com.talenteval.talenteval.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionSeeder implements CommandLineRunner {

    private final QuestionRepository questionRepository;

    @Override
    public void run(String... args) {
        if (questionRepository.count() > 0) {
            return;
        }

        List<Question> questions = List.of(
                // HR
                q("Tell me about yourself and your career journey.", InterviewRole.HR, "Behavioral", Difficulty.EASY),
                q("Describe a time you handled a conflict at work.", InterviewRole.HR, "Conflict Resolution", Difficulty.MEDIUM),
                q("What is your approach to giving constructive feedback?", InterviewRole.HR, "Leadership", Difficulty.MEDIUM),
                q("How do you prioritize tasks when everything feels urgent?", InterviewRole.HR, "Time Management", Difficulty.EASY),
                q("Describe a situation where you had to adapt to a major change.", InterviewRole.HR, "Adaptability", Difficulty.HARD),

                // UX
                q("Walk me through your design process from research to delivery.", InterviewRole.UX, "Design Process", Difficulty.MEDIUM),
                q("How do you handle stakeholder feedback that contradicts user research?", InterviewRole.UX, "Stakeholder Management", Difficulty.HARD),
                q("Explain the difference between usability testing and A/B testing.", InterviewRole.UX, "Research Methods", Difficulty.EASY),
                q("How would you redesign the checkout flow for an e-commerce app?", InterviewRole.UX, "Interaction Design", Difficulty.HARD),
                q("What accessibility standards do you follow in your designs?", InterviewRole.UX, "Accessibility", Difficulty.MEDIUM),

                // PM
                q("How do you prioritize features in a product backlog?", InterviewRole.PM, "Product Strategy", Difficulty.MEDIUM),
                q("Describe how you would launch a new product from scratch.", InterviewRole.PM, "Product Launch", Difficulty.HARD),
                q("How do you measure the success of a feature after launch?", InterviewRole.PM, "Metrics", Difficulty.MEDIUM),
                q("What frameworks do you use for making trade-off decisions?", InterviewRole.PM, "Decision Making", Difficulty.EASY),
                q("How would you handle a situation where engineering says a feature will take 3x longer than expected?", InterviewRole.PM, "Stakeholder Management", Difficulty.HARD),

                // Finance
                q("Explain the three financial statements and how they connect.", InterviewRole.FINANCE, "Financial Statements", Difficulty.EASY),
                q("Walk me through a DCF valuation.", InterviewRole.FINANCE, "Valuation", Difficulty.HARD),
                q("How would you assess whether a company should take on debt vs equity financing?", InterviewRole.FINANCE, "Corporate Finance", Difficulty.HARD),
                q("What is working capital and why does it matter?", InterviewRole.FINANCE, "Financial Analysis", Difficulty.EASY),
                q("How do you build a financial model for a subscription business?", InterviewRole.FINANCE, "Financial Modeling", Difficulty.MEDIUM),

                // Engineering
                q("Explain the difference between REST and GraphQL APIs.", InterviewRole.ENGINEERING, "System Design", Difficulty.EASY),
                q("Design a URL shortening service like bit.ly.", InterviewRole.ENGINEERING, "System Design", Difficulty.HARD),
                q("What is the difference between authentication and authorization?", InterviewRole.ENGINEERING, "Security", Difficulty.EASY),
                q("How would you optimize a slow database query?", InterviewRole.ENGINEERING, "Databases", Difficulty.MEDIUM),
                q("Explain how you would design a rate limiter for an API.", InterviewRole.ENGINEERING, "System Design", Difficulty.HARD)
        );

        questionRepository.saveAll(questions);
    }

    private Question q(String title, InterviewRole role, String topic, Difficulty difficulty) {
        return Question.builder()
                .title(title)
                .role(role)
                .topic(topic)
                .difficulty(difficulty)
                .build();
    }
}
