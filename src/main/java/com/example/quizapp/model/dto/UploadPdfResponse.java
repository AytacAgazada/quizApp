package com.example.quizapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadPdfResponse {
    private String message;
    private String subject;
    private Long pdfId;
}
