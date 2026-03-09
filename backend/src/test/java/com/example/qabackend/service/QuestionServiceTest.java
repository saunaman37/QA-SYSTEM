package com.example.qabackend.service;

import com.example.qabackend.dto.QuestionDetailResponse;
import com.example.qabackend.dto.QuestionRequest;
import com.example.qabackend.dto.QuestionResponse;
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
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock QuestionRepository questionRepository;
    @Mock AnswerRepository answerRepository;
    @InjectMocks QuestionService questionService;

    private Question buildQuestion(Long id, String title, QuestionStatus status) {
        Question q = new Question();
        q.setId(id);
        q.setTitle(title);
        q.setContent("content_" + id);
        q.setQuestioner("questioner_" + id);
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

    private QuestionRequest buildRequest(String title, String content, String questioner) {
        QuestionRequest req = new QuestionRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setQuestioner(questioner);
        return req;
    }

    // #1: 未削除質問が3件存在する場合、全件リストが返る
    @Test
    void getQuestions_noParams_returnsAll() {
        List<Question> questions = List.of(
            buildQuestion(1L, "Q1", QuestionStatus.UNANSWERED),
            buildQuestion(2L, "Q2", QuestionStatus.ANSWERED),
            buildQuestion(3L, "Q3", QuestionStatus.UNANSWERED)
        );
        when(questionRepository.findByDeletedAtIsNull(any(Sort.class))).thenReturn(questions);

        List<QuestionResponse> result = questionService.getQuestions(null, null, null);

        assertThat(result).hasSize(3);
    }

    // #2: keywordあり → 2件返る
    @Test
    void getQuestions_withKeyword_returnsFiltered() {
        List<Question> filtered = List.of(
            buildQuestion(1L, "Spring入門", QuestionStatus.UNANSWERED),
            buildQuestion(2L, "SpringBoot基礎", QuestionStatus.ANSWERED)
        );
        when(questionRepository.findByKeyword(eq("Spring"), any(Sort.class))).thenReturn(filtered);

        List<QuestionResponse> result = questionService.getQuestions("Spring", null, null);

        assertThat(result).hasSize(2);
    }

    // #3: statusあり → 1件返る
    @Test
    void getQuestions_withStatus_returnsFiltered() {
        List<Question> filtered = List.of(buildQuestion(1L, "Q1", QuestionStatus.UNANSWERED));
        when(questionRepository.findByDeletedAtIsNullAndStatus(eq(QuestionStatus.UNANSWERED), any(Sort.class)))
            .thenReturn(filtered);

        List<QuestionResponse> result = questionService.getQuestions(null, "UNANSWERED", null);

        assertThat(result).hasSize(1);
    }

    // #4: keyword + status → 1件返る
    @Test
    void getQuestions_withKeywordAndStatus_returnsFiltered() {
        List<Question> filtered = List.of(buildQuestion(1L, "Spring入門", QuestionStatus.ANSWERED));
        when(questionRepository.findByKeywordAndStatus(eq("Spring"), eq(QuestionStatus.ANSWERED), any(Sort.class)))
            .thenReturn(filtered);

        List<QuestionResponse> result = questionService.getQuestions("Spring", "ANSWERED", null);

        assertThat(result).hasSize(1);
    }

    // #5: sort=asc → ASC ソートでリポジトリが呼ばれる
    @Test
    void getQuestions_sortAsc_appliesAscSort() {
        when(questionRepository.findByDeletedAtIsNull(any(Sort.class))).thenReturn(List.of());

        questionService.getQuestions(null, null, "asc");

        verify(questionRepository).findByDeletedAtIsNull(Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    // #6: sort=desc → DESC ソートでリポジトリが呼ばれる
    @Test
    void getQuestions_sortDesc_appliesDescSort() {
        when(questionRepository.findByDeletedAtIsNull(any(Sort.class))).thenReturn(List.of());

        questionService.getQuestions(null, null, "desc");

        verify(questionRepository).findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // #7: sort=invalid → BusinessException
    @Test
    void getQuestions_invalidSort_throwsBusinessException() {
        assertThatThrownBy(() -> questionService.getQuestions(null, null, "invalid"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("sort");
    }

    // #8: status=UNKNOWN → BusinessException
    @Test
    void getQuestions_invalidStatus_throwsBusinessException() {
        assertThatThrownBy(() -> questionService.getQuestions(null, "UNKNOWN", null))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("status");
    }

    // #9: 質問詳細取得（回答2件）
    @Test
    void getQuestion_found_returnsDetail() {
        Question q = buildQuestion(1L, "Test", QuestionStatus.UNANSWERED);
        Answer a1 = buildAnswer(10L, q);
        Answer a2 = buildAnswer(11L, q);
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(q));
        when(answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(1L))
            .thenReturn(List.of(a1, a2));

        QuestionDetailResponse result = questionService.getQuestion(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAnswers()).hasSize(2);
    }

    // #10: 存在しないid → ResourceNotFoundException（メッセージに99を含む）
    @Test
    void getQuestion_notFound_throwsResourceNotFoundException() {
        when(questionRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.getQuestion(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("99");
    }

    // #11: 質問作成 → status=UNANSWERED
    @Test
    void createQuestion_valid_returnsResponse() {
        Question saved = buildQuestion(1L, "T", QuestionStatus.UNANSWERED);
        when(questionRepository.save(any(Question.class))).thenReturn(saved);

        QuestionResponse result = questionService.createQuestion(buildRequest("T", "C", "Q"));

        assertThat(result.getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
    }

    // #12: 質問更新 → タイトルが変更される
    @Test
    void updateQuestion_found_returnsUpdated() {
        Question existing = buildQuestion(1L, "Old", QuestionStatus.UNANSWERED);
        Question updated = buildQuestion(1L, "NewT", QuestionStatus.UNANSWERED);
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(questionRepository.saveAndFlush(any(Question.class))).thenReturn(updated);

        QuestionResponse result = questionService.updateQuestion(1L, buildRequest("NewT", "C", "Q"));

        assertThat(result.getTitle()).isEqualTo("NewT");
    }

    // #13: 存在しないid更新 → ResourceNotFoundException
    @Test
    void updateQuestion_notFound_throwsResourceNotFoundException() {
        when(questionRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.updateQuestion(99L, buildRequest("T", "C", "Q")))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // #14: 論理削除 → 質問・回答の deletedAt が非null になる
    @Test
    void deleteQuestion_softDeletesQuestionAndAnswers() {
        Question question = buildQuestion(1L, "Q", QuestionStatus.UNANSWERED);
        Answer a1 = buildAnswer(10L, question);
        Answer a2 = buildAnswer(11L, question);
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(question));
        when(answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByCreatedAtAsc(1L))
            .thenReturn(List.of(a1, a2));

        questionService.deleteQuestion(1L);

        assertThat(question.getDeletedAt()).isNotNull();
        assertThat(a1.getDeletedAt()).isNotNull();
        assertThat(a2.getDeletedAt()).isNotNull();
    }

    // #15: 存在しないid削除 → ResourceNotFoundException
    @Test
    void deleteQuestion_notFound_throwsResourceNotFoundException() {
        when(questionRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.deleteQuestion(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    // #51: keyword="" → 全件返る（空文字はキーワードとして扱わない）
    @Test
    void getQuestions_emptyKeyword_treatedAsNoFilter() {
        List<Question> all = List.of(
            buildQuestion(1L, "Q1", QuestionStatus.UNANSWERED),
            buildQuestion(2L, "Q2", QuestionStatus.UNANSWERED),
            buildQuestion(3L, "Q3", QuestionStatus.UNANSWERED)
        );
        when(questionRepository.findByDeletedAtIsNull(any(Sort.class))).thenReturn(all);

        List<QuestionResponse> result = questionService.getQuestions("", null, null);

        assertThat(result).hasSize(3);
    }

    // #52: keyword="   " → 全件返る（空白のみはキーワードとして扱わない）
    @Test
    void getQuestions_blankKeyword_treatedAsNoFilter() {
        List<Question> all = List.of(
            buildQuestion(1L, "Q1", QuestionStatus.UNANSWERED),
            buildQuestion(2L, "Q2", QuestionStatus.UNANSWERED),
            buildQuestion(3L, "Q3", QuestionStatus.UNANSWERED)
        );
        when(questionRepository.findByDeletedAtIsNull(any(Sort.class))).thenReturn(all);

        List<QuestionResponse> result = questionService.getQuestions("   ", null, null);

        assertThat(result).hasSize(3);
    }

    // #53: 一致なし → 空リスト（例外なし）
    @Test
    void getQuestions_noMatchingResults_returnsEmptyList() {
        when(questionRepository.findByKeyword(eq("NoMatch"), any(Sort.class))).thenReturn(List.of());

        List<QuestionResponse> result = questionService.getQuestions("NoMatch", null, null);

        assertThat(result).isEmpty();
    }

    // #54: sort=null → DESC ソートがデフォルト
    @Test
    void getQuestions_nullSort_defaultsToDesc() {
        when(questionRepository.findByDeletedAtIsNull(any(Sort.class))).thenReturn(List.of());

        questionService.getQuestions(null, null, null);

        verify(questionRepository).findByDeletedAtIsNull(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    // #55: createQuestion → エンティティに正しいフィールドが設定される
    @Test
    void createQuestion_capturesEntityWithCorrectFields() {
        when(questionRepository.save(any(Question.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        questionService.createQuestion(buildRequest("T", "C", "Q"));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).save(captor.capture());
        Question saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("T");
        assertThat(saved.getContent()).isEqualTo("C");
        assertThat(saved.getQuestioner()).isEqualTo("Q");
        assertThat(saved.getStatus()).isEqualTo(QuestionStatus.UNANSWERED);
        assertThat(saved.getDeletedAt()).isNull();
    }

    // #56: updateQuestion → エンティティに更新フィールドが反映される
    @Test
    void updateQuestion_capturesEntityWithUpdatedFields() {
        Question existing = buildQuestion(1L, "Old", QuestionStatus.UNANSWERED);
        when(questionRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(existing));
        when(questionRepository.saveAndFlush(any(Question.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        questionService.updateQuestion(1L, buildRequest("NewT", "NewC", "NewQ"));

        ArgumentCaptor<Question> captor = ArgumentCaptor.forClass(Question.class);
        verify(questionRepository).saveAndFlush(captor.capture());
        Question saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("NewT");
        assertThat(saved.getContent()).isEqualTo("NewC");
        assertThat(saved.getQuestioner()).isEqualTo("NewQ");
    }
}
