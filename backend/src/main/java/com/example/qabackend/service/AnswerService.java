package com.example.qabackend.service;

import com.example.qabackend.dto.AnswerRequest;
import com.example.qabackend.dto.AnswerResponse;
import com.example.qabackend.entity.Answer;
import com.example.qabackend.entity.Question;
import com.example.qabackend.entity.QuestionStatus;
import com.example.qabackend.exception.BusinessException;
import com.example.qabackend.exception.ResourceNotFoundException;
import com.example.qabackend.repository.AnswerRepository;
import com.example.qabackend.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@Transactional
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    public AnswerResponse createAnswer(Long questionId, AnswerRequest req) {
        Question question = questionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("質問が見つかりません。id=" + questionId));

        long count = answerRepository.countByQuestionIdAndDeletedAtIsNull(questionId);
        if (count >= 3) {
            throw new BusinessException("回答は最大3件までです。");
        }

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setContent(req.getContent());
        answer.setResponder(req.getResponder());
        Answer saved = answerRepository.save(answer);

        long afterCount = answerRepository.countByQuestionIdAndDeletedAtIsNull(questionId);
        if (afterCount == 1) {
            question.setStatus(QuestionStatus.ANSWERED);
            questionRepository.save(question);
        }

        return new AnswerResponse(saved);
    }

    public AnswerResponse updateAnswer(Long answerId, AnswerRequest req) {
        Answer answer = answerRepository.findByIdAndDeletedAtIsNull(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("回答が見つかりません。id=" + answerId));

        Long questionId = answer.getQuestion().getId();
        questionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("質問が見つかりません。id=" + questionId));

        answer.setContent(req.getContent());
        answer.setResponder(req.getResponder());
        Answer saved = answerRepository.saveAndFlush(answer);

        return new AnswerResponse(saved);
    }

    public void deleteAnswer(Long answerId) {
        Answer answer = answerRepository.findByIdAndDeletedAtIsNull(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("回答が見つかりません。id=" + answerId));

        Long questionId = answer.getQuestion().getId();
        Question question = questionRepository.findByIdAndDeletedAtIsNull(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("質問が見つかりません。id=" + questionId));

        answer.setDeletedAt(LocalDateTime.now(ZoneId.of("Asia/Tokyo")));
        answerRepository.save(answer);

        long afterCount = answerRepository.countByQuestionIdAndDeletedAtIsNull(questionId);
        if (afterCount == 0) {
            question.setStatus(QuestionStatus.UNANSWERED);
            questionRepository.save(question);
        }
    }
}
