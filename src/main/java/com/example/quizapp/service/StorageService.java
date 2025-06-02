package com.example.quizapp.service;

import com.example.quizapp.model.entity.PdfDocument;
import com.example.quizapp.repository.PdfDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final PdfProcessorService pdfProcessorService;
    private final PdfDocumentRepository pdfDocumentRepository;

    public PdfDocument storePdf(MultipartFile file) throws IOException {
        return pdfProcessorService.processPdf(file);
    }

    public List<PdfDocument> getAllSubjects() {
        return pdfDocumentRepository.findAll();
    }
}