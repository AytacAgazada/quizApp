package com.example.quizapp.model.dto;

import lombok.Data;

@Data
public class QuizConfigDto {
    private String userName;
    private int entryScore;
    private String subject;
    private int numberOfQuestions;

    // Yeni əlavə olunan field-lər
    private int startIndex;
    private int endIndex;
}
