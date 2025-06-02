package com.example.quizapp.repository;

import com.example.quizapp.model.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query(value = "SELECT q FROM Question q WHERE q.pdfDocument.subject = :subject ORDER BY RANDOM() LIMIT :limit")
    List<Question> findRandomQuestionsBySubject(String subject, int limit);

    List<Question> findByPdfDocumentId(Long pdfDocumentId);
}
