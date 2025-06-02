package com.example.quizapp.repository;

import com.example.quizapp.model.entity.PdfDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PdfDocumentRepository extends JpaRepository<PdfDocument, Long> {
    Optional<PdfDocument> findByFirstLineContent(String firstLineContent);
    Optional<PdfDocument> findBySubject(String subject);
}