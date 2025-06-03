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

        List<Question> allQuestionsForPdf = questionRepository.findByPdfDocumentId(pdfDocumentOptional.get().getId());

        // Validate the range
        if (startIndex < 0 || startIndex >= endIndex || endIndex > allQuestionsForPdf.size() || numberOfQuestions <= 0) {
            throw new IllegalArgumentException("Invalid quiz configuration: check start/end index or number of questions.");
        }

        // Get questions within the specified range
        List<Question> questionsInDesiredRange = new ArrayList<>(allQuestionsForPdf.subList(startIndex, endIndex));

        // Shuffle the questions in the desired range to randomize selection
        Collections.shuffle(questionsInDesiredRange);

        // Determine the actual number of questions to select, not exceeding the available count in the range
        int questionsToSelect = Math.min(numberOfQuestions, questionsInDesiredRange.size());

        // Select the unique questions from the shuffled list
        List<Question> selectedQuestions = questionsInDesiredRange.subList(0, questionsToSelect);

        // Shuffle options for each selected question
        selectedQuestions.forEach(q -> Collections.shuffle(q.getOptions()));

        return selectedQuestions;
    }

    // Yeni metod: evaluateQuiz ilkin balı qəbul edir
    @Transactional(readOnly = true)
    public QuizResultDto evaluateQuiz(QuizAttemptDto attempt, int initialScore) {
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
                            " (Doğru cavab: " +
                            question.getOptions().stream()
                                    .filter(Option::isCorrect)
                                    .findFirst()
                                    .map(Option::getOptionText)
                                    .orElse("N/A") + ")");
                }
            }
        }

        // Hesablamanın hər sual üçün 1 bal olduğunu fərz edək
        // Ümumi sualların sayı 100 bal üzərindən necə hesablanacağını müəyyənləşdirmək lazımdır.
        // Hər sualın çəkisi bərabərdirsə: (doğru cavablar / ümumi suallar) * (100 - initialScore) + initialScore
        double scorePerQuestion = (totalQuestions > 0) ? (double) (100 - initialScore) / totalQuestions : 0;
        int calculatedScore = (int) Math.round(correctAnswers * scorePerQuestion);
        int finalScore = initialScore + calculatedScore;

        // Balın 100-ü keçməməsini təmin edirik
        if (finalScore > 100) {
            finalScore = 100;
        }

        return new QuizResultDto(correctAnswers, totalQuestions, incorrectQuestions, finalScore);
    }
}