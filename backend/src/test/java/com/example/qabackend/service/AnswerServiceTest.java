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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock AnswerRepository answerRepository;
    @Mock QuestionRepository questionRepository;
    @InjectMocks AnswerService answerService;

    private Question buildQuestion(Long id, QuestionStatus status) {
        Question q = new Question();
        q.setId(id);
        q.setTitle("Q" + id);
        q.setContent("content");
        q.setQuestioner("questioner");
        q.setStatus(status);
        q.setCreatedAt(LocalDateTime.now());
        q.setUpdatedAt(LocalDateTime.now());
        return q;
    }

    private Answer buildAnswer(Long id, Question question) {
        Answer a = new Answer();
        a.setId(id);
        a.setQuestion(question);
        a.setContent("answer_" + id);
        a.setResponder("responder_" + id);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return a;
    }

    private AnswerRequest buildRequest(String content, String responder) {
        AnswerRequest req = new AnswerRequest();
        req.setContent(content);
        req.setResponder(responder);
        return req;
    }

    // #16: 初回回答 → 質問status が ANSWERED になる
    @Test
    void createAnswer_firstAnswer_setsQuestionStatusToAnswered() {
        Question question = buildQuestion(1L, QuestionStatus.UNANSWERED);
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        // 保存前のカウント: 0
        when(answerRepository.countByQuestionIdAndDeletedAtIsNull(1L)).thenReturn(0L).thenReturn(1L);
        when(answerRepository.save(any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        answerService.createAnswer(1L, buildRequest("Ans", "Resp"));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuestionStatus.ANSWERED);
    }

    // #17: 2回目以降の回答 → 質問statusは変わらない
    @Test
    void createAnswer_secondAnswer_statusRemainsAnswered() {
        Question question = buildQuestion(1L, QuestionStatus.ANSWERED);
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        // 保存前: 1件, 保存後: 2件
        when(answerRepository.countByQuestionIdAndDeletedAtIsNull(1L)).thenReturn(1L).thenReturn(2L);
        Answer savedAnswer = buildAnswer(10L, question);
        when(answerRepository.save(any(Answer.class))).thenReturn(savedAnswer);

        AnswerResponse result = answerService.createAnswer(1L, buildRequest("Ans", "Resp"));

        assertThat(result).isNotNull();
        // afterCount=2 なので questionRepository.save は呼ばれない
        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        // save が呼ばれていないことを確認（times(0)）
        verify(questionRepository, org.mockito.Mockito.times(0)).save(any(Question.class));
    }

    // #18: 質問が存在しない → ResourceNotFoundException
    @Test
    void createAnswer_questionNotFound_throwsResourceNotFoundException() {
        when(questionRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.createAnswer(99L, buildRequest("Ans", "Resp")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // #19: 有効回答数3件 → BusinessException（最大3件）
    @Test
    void createAnswer_maxAnswers_throwsBusinessException() {
        Question question = buildQuestion(1L, QuestionStatus.ANSWERED);
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        when(answerRepository.countByQuestionIdAndDeletedAtIsNull(1L)).thenReturn(3L);

        assertThatThrownBy(() -> answerService.createAnswer(1L, buildRequest("Ans", "Resp")))
            .isInstanceOf(BusinessException.class)
            .hasMessage("回答は最大3件までです。");
    }

    // #20: 回答更新 → content・responder が変更される
    @Test
    void updateAnswer_valid_returnsUpdated() {
        Question question = buildQuestion(1L, QuestionStatus.ANSWERED);
        Answer answer = buildAnswer(10L, question);
        when(answerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(answer));
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        when(answerRepository.saveAndFlush(any(Answer.class))).thenAnswer(inv -> inv.getArgument(0));

        AnswerResponse result = answerService.updateAnswer(10L, buildRequest("New", "Resp"));

        assertThat(result.getContent()).isEqualTo("New");
        assertThat(result.getResponder()).isEqualTo("Resp");
    }

    // #21: 存在しない回答id → ResourceNotFoundException
    @Test
    void updateAnswer_answerNotFound_throwsResourceNotFoundException() {
        when(answerRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.updateAnswer(99L, buildRequest("Ans", "Resp")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // #22: 論理削除済み質問に紐づく回答 → ResourceNotFoundException（更新不可）
    @Test
    void updateAnswer_whenParentQuestionSoftDeleted_throwsResourceNotFoundException() {
        Question question = buildQuestion(1L, QuestionStatus.ANSWERED);
        Answer answer = buildAnswer(10L, question);
        when(answerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(answer));
        // 質問が論理削除済み（findByIdAndDeletedAtIsNull が empty を返す）
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.updateAnswer(10L, buildRequest("New", "Resp")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // #23: 最後の回答を削除 → deletedAt が設定され、質問status が UNANSWERED になる
    @Test
    void deleteAnswer_lastAnswer_setsQuestionStatusToUnanswered() {
        Question question = buildQuestion(1L, QuestionStatus.ANSWERED);
        Answer answer = buildAnswer(10L, question);
        when(answerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(answer));
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        when(answerRepository.save(any(Answer.class))).thenReturn(answer);
        // 削除後: 0件
        when(answerRepository.countByQuestionIdAndDeletedAtIsNull(1L)).thenReturn(0L);

        answerService.deleteAnswer(10L);

        assertThat(answer.getDeletedAt()).isNotNull();

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
    }

    // #24: 最後でない回答を削除 → deletedAt が設定され、質問statusは変わらない
    @Test
    void deleteAnswer_notLastAnswer_statusRemainsAnswered() {
        Question question = buildQuestion(1L, QuestionStatus.ANSWERED);
        Answer answer = buildAnswer(10L, question);
        when(answerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(answer));
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        when(answerRepository.save(any(Answer.class))).thenReturn(answer);
        // 削除後: まだ1件残っている
        when(answerRepository.countByQuestionIdAndDeletedAtIsNull(1L)).thenReturn(1L);

        answerService.deleteAnswer(10L);

        assertThat(answer.getDeletedAt()).isNotNull();
        verify(questionRepository, org.mockito.Mockito.times(0)).save(any(Question.class));
    }

    // #25: 存在しない回答id削除 → ResourceNotFoundException
    @Test
    void deleteAnswer_answerNotFound_throwsResourceNotFoundException() {
        when(answerRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> answerService.deleteAnswer(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // #57: deleteAnswer（最後の回答）→ questionRepository.save が UNANSWERED で呼ばれる
    @Test
    void deleteAnswer_lastAnswer_verifiesQuestionSavedWithUnanswered() {
        Question question = buildQuestion(1L, QuestionStatus.ANSWERED);
        Answer answer = buildAnswer(10L, question);
        when(answerRepository.findByIdAndDeletedAtIsNull(10L)).thenReturn(Optional.of(answer));
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        when(answerRepository.save(any(Answer.class))).thenReturn(answer);
        when(answerRepository.countByQuestionIdAndDeletedAtIsNull(1L)).thenReturn(0L);

        answerService.deleteAnswer(10L);

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
    }
}
