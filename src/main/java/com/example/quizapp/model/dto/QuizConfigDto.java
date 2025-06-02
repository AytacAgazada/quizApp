package com.example.quizapp.model.dto;

import lombok.Data;

@Data
public class QuizConfigDto {
    private String userName;
    private int entryScore; // Giriş balı (bu məntiq hazırda istifadə olunmur, amma əlavə edilib)
    private String subject; // Seçilmiş fənn
    private int numberOfQuestions;
    // İstədiyiniz sual aralığı (başlanğıc və son) bu nümunədə istifadə olunmur,
    // lakin numberOfQuestions ilə random olaraq seçilir.
    // Əgər spesifik aralıq tələb olunursa, əlavə dəyişikliklər tələb olunur.
}