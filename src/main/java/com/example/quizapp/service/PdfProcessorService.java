package com.example.quizapp.service;

import com.example.quizapp.model.entity.Option;
import com.example.quizapp.model.entity.PdfDocument;
import com.example.quizapp.model.entity.Question;
import com.example.quizapp.repository.PdfDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PdfProcessorService {

    private final PdfDocumentRepository pdfDocumentRepository;

    @Transactional
    public PdfDocument processPdf(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        // PDF-in ilk cümləsini fənn adı kimi götürürük
        String firstLine = text.split("\n")[0].trim();

        // Check if PDF with this first line already exists
        Optional<PdfDocument> existingPdf = pdfDocumentRepository.findByFirstLineContent(firstLine);
        if (existingPdf.isPresent()) {
            return existingPdf.get(); // If exists, return existing document
        }

        PdfDocument pdfDocument = new PdfDocument();
        pdfDocument.setFileName(file.getOriginalFilename());
        pdfDocument.setFirstLineContent(firstLine);
        pdfDocument.setSubject(firstLine); // Subject is the first line content

        List<Question> questions = parseQuestionsFromText(text, pdfDocument);
        pdfDocument.setQuestions(questions);

        return pdfDocumentRepository.save(pdfDocument);
    }

    private List<Question> parseQuestionsFromText(String text, PdfDocument pdfDocument) {
        List<Question> questions = new ArrayList<>();
        String[] lines = text.split("\\r?\\n"); // Mətni sətirlərə ayırırıq

        int i = 0;
        while (i < lines.length) {
            String line = lines[i].trim();

            // Sətirin sual nömrəsi ilə (məsələn, "1.") başlayıb-başlamadığını yoxlayırıq
            if (line.matches("^\\d+\\..*")) {
                StringBuilder questionTextBuilder = new StringBuilder();
                questionTextBuilder.append(line);
                i++; // Növbəti sətrə keçirik

                // Sual mətninin davam edən sətirlərini toplayırıq
                // Yeni sual, seçim və ya sənədin sonuna çatana qədər davam edirik
                while (i < lines.length &&
                        !lines[i].trim().matches("^\\d+\\..*") &&     // Yeni sualın başlanğıcı deyil
                        !lines[i].trim().startsWith("•") &&         // Seçim işarəsi (bullet point) deyil
                        !lines[i].trim().startsWith("√") &&         // Seçim işarəsi (checkmark) deyil
                        !lines[i].trim().startsWith("-") &&         // Seçim işarəsi (tire) deyil
                        !lines[i].trim().isEmpty()) {               // Boş sətir deyil
                    String extraLine = lines[i].trim();
                    questionTextBuilder.append(" ").append(extraLine);
                    i++;
                }

                Question question = new Question();
                question.setQuestionText(questionTextBuilder.toString().trim());
                question.setPdfDocument(pdfDocument);

                List<Option> options = new ArrayList<>();
                while (i < lines.length) {
                    String optionLine = lines[i].trim();
                    if (optionLine.matches("^\\d+\\..*")) {
                        break; // Növbəti sualın başlanğıcıdır
                    }

                    if (optionLine.startsWith("•") || optionLine.startsWith("√") || optionLine.startsWith("-")) {
                        boolean isCorrect = optionLine.startsWith("√");
                        String optionContent = optionLine.substring(1).trim();

                        Option option = new Option();
                        option.setOptionText(optionContent);
                        option.setCorrect(isCorrect);
                        option.setQuestion(question);
                        options.add(option);
                        i++;
                    } else {
                        // Əgər sətir seçim və ya yeni sual deyilsə, mövcud seçimin davamı ola bilər.
                        // Lakin sadəlik üçün, indikatorla başlamayan sətirləri keçirik.
                        break;
                    }
                }
                question.setOptions(options);
                // Seçimlər olmadan sual əlavə etməmək üçün əlavə yoxlama
                if (!options.isEmpty()) {
                    questions.add(question);
                } else {
                    System.err.println("Xəbərdarlıq: Seçimləri olmayan sual keçildi: " + question.getQuestionText());
                }
            } else {
                i++; // Sual olmayan sətirləri keçirik
            }
        }
        return questions;
    }

    private int findFirstOptionIndex(String text) {
        // Bu metod yuxarıdakı sətir əsaslı parsinq ilə artıq istifadə edilmir, lakin əvvəlki kodunuzda olduğu üçün saxlanılıb.
        // Onu istifadə etmək lazım deyil.
        Pattern optionStartPattern = Pattern.compile("•|√|-|\\d+\\.");
        Matcher matcher = optionStartPattern.matcher(text);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    private List<Option> parseOptions(String optionsText, Question question) {
        // Bu metod yuxarıdakı sətir əsaslı parsinq ilə artıq istifadə edilmir, lakin əvvəlki kodunuzda olduğu üçün saxlanılıb.
        // Onu istifadə etmək lazım deyil.
        List<Option> options = new ArrayList<>();
        if (optionsText.isEmpty()) {
            return options;
        }

        Pattern optionLinePattern = Pattern.compile("(•|√|-)\\s*(.*?)(?=\\n(?:•|√|-)|\\Z)", Pattern.DOTALL);
        Matcher optionMatcher = optionLinePattern.matcher(optionsText);

        while (optionMatcher.find()) {
            String indicator = optionMatcher.group(1);
            String optionContent = optionMatcher.group(2).trim();

            Option option = new Option();
            option.setOptionText(optionContent.replaceAll("[\r\n]+", " ").trim());
            option.setCorrect("√".equals(indicator));
            option.setQuestion(question);
            options.add(option);
        }
        return options;
    }
}