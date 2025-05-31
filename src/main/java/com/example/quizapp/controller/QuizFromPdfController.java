package com.example.quizapp.controller;

import com.example.quizapp.model.dto.QuizDto;
import com.example.quizapp.service.PdfQuestionService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pdfquiz")
@Validated
@RequiredArgsConstructor
public class QuizFromPdfController {

    private final PdfQuestionService pdfQuestionService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAndExtractQuestions(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("subject") @NotBlank String subject,
            @RequestParam("questionCount") @Min(1) int questionCount,
            @RequestParam("startQuestion") @Min(1) int startQuestion,
            @RequestParam("endQuestion") @NotNull String endQuestion
    ) {
        try {
            int endQuestionStr;
            if ("last".equalsIgnoreCase(endQuestion)) {
                endQuestionStr = Integer.MAX_VALUE; // PdfQuestionService içində max-lanır lines.size()-a
            } else {
                endQuestionStr = Integer.parseInt(endQuestion);
            }

            List<QuizDto> questions = pdfQuestionService.savePdfAndExtractQuestions(
                    file, subject, questionCount, startQuestion, endQuestionStr);

            return ResponseEntity.ok(questions);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("End question must be a number or 'last'");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid input: " + e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Internal error: " + e.getMessage());
        }
    }
}
