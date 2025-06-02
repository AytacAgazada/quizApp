package com.example.quizapp.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String subject; // Fənn adı
    private String firstLineContent; // PDF-in ilk cümləsi fənn adı kimi saxlanılır

    @OneToMany(mappedBy = "pdfDocument", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}
