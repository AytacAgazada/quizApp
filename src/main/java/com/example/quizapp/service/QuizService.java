package com.example.quizapp.service;

import com.example.quizapp.model.dto.QuizAttemptDto;
import com.example.quizapp.model.dto.QuizResultDto;
import com.example.quizapp.model.entity.Option;
import com.example.quizapp.model.entity.PdfDocument;
import com.example.quizapp.model.entity.Question;
import com.example.quizapp.repository.PdfDocumentRepository;
import com.example.quizapp.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuestionRepository questionRepository;
    private final PdfDocumentRepository pdfDocumentRepository;

    @Transactional(readOnly = true)
    public List<Question> generateQuiz(String subject, int numberOfQuestions, int startIndex, int endIndex) {
        Optional<PdfDocument> pdfDocumentOptional = pdfDocumentRepository.findBySubject(subject);
        if (pdfDocumentOptional.isEmpty()) {
            throw new IllegalArgumentException("Subject not found: " + subject);
        }

        List<Question> allQuestions = questionRepository.findByPdfDocumentId(pdfDocumentOptional.get().getId());

        // Aralığın düzgünlüyünü yoxla
        if (startIndex < 0 || endIndex > allQuestions.size() || startIndex >= endIndex) {
            throw new IllegalArgumentException("Invalid question range.");
        }

        List<Question> rangedQuestions = allQuestions.subList(startIndex, endIndex);
        Collections.shuffle(rangedQuestions);

        // Sorğu sayını aşmamaq üçün limit qoy
        int limit = Math.min(numberOfQuestions, rangedQuestions.size());
        List<Question> selectedQuestions = rangedQuestions.subList(0, limit);

        selectedQuestions.forEach(q -> Collections.shuffle(q.getOptions()));
        return selectedQuestions;
    }

    @Transactional(readOnly = true)
    public QuizResultDto evaluateQuiz(QuizAttemptDto attempt) {
        int correctAnswers = 0;
        int totalQuestions = attempt.getAnswers().size();
        List<String> incorrectQuestions = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : attempt.getAnswers().entrySet()) {
            Long questionId = entry.getKey();
            Long selectedOptionId = entry.getValue();

            Optional<Question> questionOptional = questionRepository.findById(questionId);
            if (questionOptional.isPresent()) {
                Question question = questionOptional.get();
                boolean isCorrect = question.getOptions().stream()
                        .anyMatch(option -> option.getId().equals(selectedOptionId) && option.isCorrect());

                if (isCorrect) {
                    correctAnswers++;
                } else {
                    incorrectQuestions.add(question.getQuestionText() +
                            " (Correct answer: " +
                            question.getOptions().stream()
                                    .filter(Option::isCorrect)
                                    .findFirst()
                                    .map(Option::getOptionText)
                                    .orElse("N/A") + ")");
                }
            }
        }

        return new QuizResultDto(correctAnswers, totalQuestions, incorrectQuestions);
    }
}
