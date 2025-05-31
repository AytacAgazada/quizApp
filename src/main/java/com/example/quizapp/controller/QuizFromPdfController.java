package com.example.quizapp.controller;

import com.example.quizapp.model.dto.QuizDto;
import com.example.quizapp.model.dto.UserAnswer;
import com.example.quizapp.model.dto.QuizResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
@Validated
public class QuizController {

    private final QuizService service;

    public QuizController(QuizService service) {
        this.service = service;
    }

    @GetMapping("/questions")
    public ResponseEntity<List<QuizDto>> getQuestions(@RequestParam(defaultValue = "30") int count) {
        return ResponseEntity.ok(service.getQuestions(count));
    }

    @PostMapping("/check")
    public ResponseEntity<List<QuizResult>> checkAnswers(@Valid @RequestBody List<@Valid UserAnswer> answers) {
        return ResponseEntity.ok(service.checkAnswers(answers));
    }
}


