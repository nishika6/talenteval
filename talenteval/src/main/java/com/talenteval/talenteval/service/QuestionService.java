package com.talenteval.talenteval.service;

import com.talenteval.talenteval.dto.QuestionRequest;
import com.talenteval.talenteval.dto.QuestionResponse;
import com.talenteval.talenteval.entity.Difficulty;
import com.talenteval.talenteval.entity.InterviewRole;
import com.talenteval.talenteval.entity.Question;
import com.talenteval.talenteval.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public List<QuestionResponse> getAllQuestions(String role, String topic) {
        List<Question> questions;

        if (role != null && topic != null) {
            questions = questionRepository.findByRoleAndTopicContainingIgnoreCase(
                    InterviewRole.valueOf(role), topic);
        } else if (role != null) {
            questions = questionRepository.findByRole(InterviewRole.valueOf(role));
        } else if (topic != null) {
            questions = questionRepository.findByTopicContainingIgnoreCase(topic);
        } else {
            questions = questionRepository.findAll();
        }

        return questions.stream().map(this::toResponse).toList();
    }

    public QuestionResponse getQuestion(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        return toResponse(question);
    }

    public QuestionResponse createQuestion(QuestionRequest request) {
        Question question = Question.builder()
                .title(request.getTitle())
                .role(InterviewRole.valueOf(request.getRole()))
                .topic(request.getTopic())
                .difficulty(Difficulty.valueOf(request.getDifficulty()))
                .timeLimit(request.getTimeLimit() != null ? request.getTimeLimit() : 120)
                .build();

        questionRepository.save(question);
        return toResponse(question);
    }

    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        question.setTitle(request.getTitle());
        question.setRole(InterviewRole.valueOf(request.getRole()));
        question.setTopic(request.getTopic());
        question.setDifficulty(Difficulty.valueOf(request.getDifficulty()));
        question.setTimeLimit(request.getTimeLimit() != null ? request.getTimeLimit() : 120);

        questionRepository.save(question);
        return toResponse(question);
    }

    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new IllegalArgumentException("Question not found");
        }
        questionRepository.deleteById(id);
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getTitle(),
                question.getRole().name(),
                question.getTopic(),
                question.getDifficulty().name(),
                question.getTimeLimit()
        );
    }
}
