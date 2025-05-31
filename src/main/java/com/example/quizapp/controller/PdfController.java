package com.example.quizapp.controller;

import com.example.quizapp.service.PdfStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfStorageService storageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("PDF upload failed: empty or null file");
            return ResponseEntity.badRequest().body("Fayl göndərilməyib və ya boşdur.");
        }

        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            log.warn("PDF upload failed: wrong content type {}", file.getContentType());
            return ResponseEntity.badRequest().body("Yalnız PDF faylları qəbul olunur.");
        }

        try {
            String path = storageService.saveFile(file);
            log.info("PDF uploaded successfully: {}", path);
            return ResponseEntity.ok("PDF yadda saxlanıldı: " + path);
        } catch (Exception e) {
            log.error("PDF upload error: ", e);
            return ResponseEntity.status(500).body("Fayl saxlanılarkən xəta: " + e.getMessage());
        }
    }
}
