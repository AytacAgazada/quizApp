package com.example.quizapp.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class QuizDto {

    private Long id;

    @NotBlank(message = "Question text cannot be empty")
    @Size(min = 10, max = 1000, message = "Question text must be between 10 and 1000 characters")
    private String questionText;

    @Size(min = 2, max = 10, message = "There must be between 2 and 10 answer options")
    @NotNull(message = "Answers list cannot be null")
    private List<@NotBlank(message = "Answer option cannot be blank") String> answers;

    @Min(value = 0, message = "Correct index must be 0 or higher")
    private int correctIndex;

    @NotBlank(message = "Subject cannot be blank")
    private String subject;
}
