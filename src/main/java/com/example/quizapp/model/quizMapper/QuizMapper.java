package com.example.quizapp.model.quizMapper;

import com.example.quizapp.model.dto.QuizDto;
import com.example.quizapp.model.entity.QuizEntity;

public class QuizMapper {

    public static QuizDto toDTO(QuizEntity entity) {
        if (entity == null) return null;
        QuizDto dto = new QuizDto();
        dto.setId(entity.getId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setAnswers(entity.getAnswers());
        dto.setCorrectIndex(entity.getCorrectIndex());
        dto.setSubject(entity.getSubject());
        return dto;
    }

    public static QuizEntity toEntity(QuizDto dto) {
        if (dto == null) return null;
        QuizEntity entity = new QuizEntity();
        entity.setId(dto.getId());
        entity.setQuestionText(dto.getQuestionText());
        entity.setAnswers(dto.getAnswers());
        entity.setCorrectIndex(dto.getCorrectIndex());
        entity.setSubject(dto.getSubject());
        return entity;
    }
}
