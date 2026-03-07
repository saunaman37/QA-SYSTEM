package com.example.qabackend.service;

import com.example.qabackend.dto.AnswerResponse;
import com.example.qabackend.dto.QuestionDetailResponse;
import com.example.qabackend.dto.QuestionRequest;
import com.example.qabackend.dto.QuestionResponse;
import com.example.qabackend.entity.Question;
import com.example.qabackend.entity.QuestionStatus;
import com.example.qabackend.exception.BusinessException;
import com.example.qabackend.exception.ResourceNotFoundException;
import com.example.qabackend.repository.AnswerRepository;
import com.example.qabackend.repository.QuestionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public QuestionService(QuestionRepository questionRepository, AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public List<QuestionResponse> getQuestions(String keyword, String status, String sort) {
        Sort sortOrder = resolveSort(sort);
        QuestionStatus questionStatus = resolveStatus(status);

        List<Question> questions;
        boolean hasKeyword = keyword != null && !keyword.isBlank();

        if (hasKeyword && questionStatus != null) {
            questions = questionRepository.findByKeywordAndStatus(keyword, questionStatus, sortOrder);
        } else if (hasKeyword) {
            questions = questionRepository.findByKeyword(keyword, sortOrder);
        } else if (questionStatus != null) {
            questions = questionRepository.findByDeletedAtIsNullAndStatus(questionStatus, sortOrder);
        } else {
            questions = questionRepository.findByDeletedAtIsNull(sortOrder);
        }

        return questions.stream().map(QuestionResponse::new).toList();
    }

    public QuestionDetailResponse getQuestion(Long id) {
        Question question = questionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("質問が見つかりません。id=" + id));

        List<AnswerResponse> answers = answerRepository
                .findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(id)
                .stream()
                .map(AnswerResponse::new)
                .toList();

        return new QuestionDetailResponse(question, answers);
    }

    public QuestionResponse createQuestion(QuestionRequest req) {
        Question question = new Question();
        question.setTitle(req.getTitle());
        question.setContent(req.getContent());
        question.setQuestioner(req.getQuestioner());
        question.setStatus(QuestionStatus.UNANSWERED);
        return new QuestionResponse(questionRepository.save(question));
    }

    public QuestionResponse updateQuestion(Long id, QuestionRequest req) {
        Question question = questionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("質問が見つかりません。id=" + id));

        question.setTitle(req.getTitle());
        question.setContent(req.getContent());
        question.setQuestioner(req.getQuestioner());
        return new QuestionResponse(questionRepository.save(question));
    }

    public void deleteQuestion(Long id) {
        Question question = questionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("質問が見つかりません。id=" + id));

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));

        answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(id)
                .forEach(answer -> {
                    answer.setDeletedAt(now);
                    answerRepository.save(answer);
                });

        question.setDeletedAt(now);
        questionRepository.save(question);
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank() || sort.equals("desc")) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        } else if (sort.equals("asc")) {
            return Sort.by(Sort.Direction.ASC, "createdAt");
        } else {
            throw new BusinessException("sort パラメータが不正です。asc または desc を指定してください。");
        }
    }

    private QuestionStatus resolveStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return QuestionStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("status パラメータが不正です。UNANSWERED または ANSWERED を指定してください。");
        }
    }
}
