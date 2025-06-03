package com.example.quizapp.controller;

import com.example.quizapp.model.dto.QuizAttemptDto;
import com.example.quizapp.model.dto.QuizConfigDto;
import com.example.quizapp.model.dto.QuizResultDto;
import com.example.quizapp.model.dto.UploadPdfResponse;
import com.example.quizapp.model.entity.PdfDocument;
import com.example.quizapp.model.entity.Question;
import com.example.quizapp.service.QuizService;
import com.example.quizapp.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final StorageService storageService;
    private final QuizService quizService;

    @PostMapping("/upload-pdf")
    public ResponseEntity<UploadPdfResponse> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            PdfDocument pdfDocument = storageService.storePdf(file);
            return ResponseEntity.ok(new UploadPdfResponse(
                    "PDF yükləndi və uğurla emal edildi!",
                    pdfDocument.getSubject(),
                    pdfDocument.getId()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadPdfResponse("PDF yükləmə və emal uğursuz oldu: " + e.getMessage(), null, null));
        }
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<String>> getAllSubjects() {
        List<String> subjects = storageService.getAllSubjects().stream()
                .map(PdfDocument::getSubject)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subjects);
    }

    @PostMapping("/generate")
    public ResponseEntity<List<Question>> generateQuiz(@RequestBody QuizConfigDto quizConfig) {
        try {
            List<Question> questions = quizService.generateQuiz(
                    quizConfig.getSubject(),
                    quizConfig.getNumberOfQuestions(),
                    quizConfig.getStartIndex(),
                    quizConfig.getEndIndex()
            );
            return ResponseEntity.ok(questions);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResultDto> submitQuiz(@RequestBody QuizAttemptDto attempt,
                                                    @RequestParam int initialScore) { // initialScore-u əlavə etdik
        QuizResultDto result = quizService.evaluateQuiz(attempt, initialScore); // initialScore-u ötürürük
        return ResponseEntity.ok(result);
    }
}