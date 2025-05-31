package com.example.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResult {
    private Long questionId;
    private String questionText;
    private int userAnswerIndex;
    private int correctAnswerIndex;
    private boolean correct;
    private List<String> answers;
}
