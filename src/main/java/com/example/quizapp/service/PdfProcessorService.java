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
        // Regular expression to find questions and options
        // It looks for a number followed by a dot (e.g., "1.") for the question,
        // and then lines starting with bullet points or similar for options.
        // It captures the question text, then all option lines until the next question number.
        Pattern questionPattern = Pattern.compile("(\\d+\\.\\s*)([\\s\\S]*?)(?=\\d+\\.\\s*|\\Z)");
        Matcher questionMatcher = questionPattern.matcher(text);

        while (questionMatcher.find()) {
            String fullMatch = questionMatcher.group(0);
            int questionNumberEndIndex = questionMatcher.group(1).length();
            String questionAndOptions = fullMatch.substring(questionNumberEndIndex).trim();

            // Split question and options
            int firstOptionIndex = findFirstOptionIndex(questionAndOptions);
            String questionText = "";
            String optionsText = "";

            if (firstOptionIndex != -1) {
                questionText = questionAndOptions.substring(0, firstOptionIndex).trim();
                optionsText = questionAndOptions.substring(firstOptionIndex).trim();
            } else {
                questionText = questionAndOptions.trim();
            }

            Question question = new Question();
            question.setQuestionText(questionText.replaceAll("[\r\n]+", " ").trim());
            question.setPdfDocument(pdfDocument);

            List<Option> options = parseOptions(optionsText, question);
            question.setOptions(options);
            questions.add(question);
        }
        return questions;
    }

    private int findFirstOptionIndex(String text) {
        Pattern optionStartPattern = Pattern.compile("•|√|-|\\d+\\."); // Common option starting characters
        Matcher matcher = optionStartPattern.matcher(text);
        if (matcher.find()) {
            return matcher.start();
        }
        return -1;
    }

    private List<Option> parseOptions(String optionsText, Question question) {
        List<Option> options = new ArrayList<>();
        if (optionsText.isEmpty()) {
            return options; // No options to parse
        }

        // Split options by lines that start with a bullet point or checkmark
        Pattern optionLinePattern = Pattern.compile("(•|√|-)\\s*(.*?)(?=\\n(?:•|√|-)|\\Z)", Pattern.DOTALL);
        Matcher optionMatcher = optionLinePattern.matcher(optionsText);

        while (optionMatcher.find()) {
            String indicator = optionMatcher.group(1);
            String optionContent = optionMatcher.group(2).trim();

            Option option = new Option();
            option.setOptionText(optionContent.replaceAll("[\r\n]+", " ").trim()); // Clean up line breaks
            option.setCorrect("√".equals(indicator)); // If starts with √, it's correct
            option.setQuestion(question);
            options.add(option);
        }
        return options;
    }
}
