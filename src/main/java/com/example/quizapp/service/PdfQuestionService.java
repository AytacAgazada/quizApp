package com.example.quizapp.service;

import com.example.quizapp.model.dto.QuizDto;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PdfQuestionService {

    private final PdfStorageService storageService;

    public List<QuizDto> savePdfAndExtractQuestions(MultipartFile file, String subject, int count, int start, int end) throws IOException {
        storageService.saveFile(file);
        return extractQuestions(subject, count, start, end);
    }

    public List<QuizDto> extractQuestions(String subject, int count, int start, int end) throws IOException {
        if (!storageService.isFileStored()) {
            throw new IllegalStateException("PDF faylı yüklənməyib!");
        }

        Path pdfPath = storageService.getStoredFile();

        String content;
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            content = stripper.getText(document);
        }

        List<String> lines = Arrays.stream(content.split("\\r?\\n"))
                .filter(line -> !line.isBlank())
                .toList();

        if (start < 1 || start > lines.size()) {
            throw new IllegalArgumentException("Start sual nömrəsi düzgün deyil.");
        }

        int effectiveEnd = (end < 0 || end > lines.size()) ? lines.size() : end;
        if (effectiveEnd < start) {
            throw new IllegalArgumentException("End sual nömrəsi start-dan kiçik ola bilməz.");
        }

        List<String> range = lines.subList(start - 1, effectiveEnd);

        Collections.shuffle(range);

        List<String> selected = range.stream()
                .limit(count)
                .toList();

        List<QuizDto> result = new ArrayList<>();
        for (String line : selected) {
            QuizDto dto = new QuizDto();
            dto.setQuestionText(line);
            dto.setAnswers(List.of("A", "B", "C", "D")); // placeholder cavab variantları
            dto.setCorrectIndex(0); // placeholder düzgün cavab indeksi
            dto.setSubject(subject);
            result.add(dto);
        }

        return result;
    }
}
