package com.example.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultDto {
    private int correctAnswers;
    private int totalQuestions;
    private List<String> incorrectQuestions;
    private int finalScore; // Yeni əlavə olunan sahə
}