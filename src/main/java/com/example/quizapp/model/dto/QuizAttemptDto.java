package com.example.quizapp.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class QuizAttemptDto {
    private String userName;
    private Map<Long, Long> answers; // Map of Question ID to Selected Option ID
}
