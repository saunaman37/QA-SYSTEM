package com.example.qabackend.controller;

import com.example.qabackend.dto.QuestionDetailResponse;
import com.example.qabackend.dto.QuestionRequest;
import com.example.qabackend.dto.QuestionResponse;
import com.example.qabackend.entity.Question;
import com.example.qabackend.entity.QuestionStatus;
import com.example.qabackend.exception.GlobalExceptionHandler;
import com.example.qabackend.exception.ResourceNotFoundException;
import com.example.qabackend.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock QuestionService questionService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new QuestionController(questionService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
    }

    private QuestionResponse buildQuestionResponse(Long id, String title, QuestionStatus status) {
        Question q = new Question();
        q.setId(id);
        q.setTitle(title);
        q.setContent("content");
        q.setQuestioner("questioner");
        q.setStatus(status);
        q.setCreatedAt(LocalDateTime.now());
        q.setUpdatedAt(LocalDateTime.now());
        return new QuestionResponse(q);
    }

    private QuestionDetailResponse buildDetailResponse(Long id, String title) {
        Question q = new Question();
        q.setId(id);
        q.setTitle(title);
        q.setContent("content");
        q.setQuestioner("questioner");
        q.setStatus(QuestionStatus.UNANSWERED);
        q.setCreatedAt(LocalDateTime.now());
        q.setUpdatedAt(LocalDateTime.now());
        return new QuestionDetailResponse(q, List.of());
    }

    private QuestionRequest buildRequest(String title, String content, String questioner) {
        QuestionRequest req = new QuestionRequest();
        req.setTitle(title);
        req.setContent(content);
        req.setQuestioner(questioner);
        return req;
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // #26: GET /api/questions → 200 + JSON配列(2要素)
    @Test
    void getQuestions_returns200() throws Exception {
        when(questionService.getQuestions(any(), any(), any()))
            .thenReturn(List.of(
                buildQuestionResponse(1L, "Q1", QuestionStatus.UNANSWERED),
                buildQuestionResponse(2L, "Q2", QuestionStatus.ANSWERED)
            ));

        mockMvc.perform(get("/api/questions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    // #27: GET /api/questions/1 → 200 + id, title, status, answers配列
    @Test
    void getQuestion_exists_returns200() throws Exception {
        when(questionService.getQuestion(1L)).thenReturn(buildDetailResponse(1L, "Test"));

        mockMvc.perform(get("/api/questions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("Test"))
            .andExpect(jsonPath("$.status").value("UNANSWERED"))
            .andExpect(jsonPath("$.answers").isArray());
    }

    // #28: GET /api/questions/99 → 404 + status=404, error="Not Found"
    @Test
    void getQuestion_notExists_returns404() throws Exception {
        when(questionService.getQuestion(99L)).thenThrow(new ResourceNotFoundException("質問が見つかりません。id=99"));

        mockMvc.perform(get("/api/questions/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // #29: POST /api/questions → 201 + id, title, status
    @Test
    void createQuestion_valid_returns201() throws Exception {
        when(questionService.createQuestion(any())).thenReturn(buildQuestionResponse(1L, "T", QuestionStatus.UNANSWERED));

        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("T", "C", "Q"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("T"))
            .andExpect(jsonPath("$.status").value("UNANSWERED"));
    }

    // #30: POST title="" → 400 + fieldErrors[title]
    @Test
    void createQuestion_missingTitle_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("", "C", "Q"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='title')]").exists());
    }

    // #31: POST content=null → 400 + fieldErrors[content]
    @Test
    void createQuestion_emptyContent_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("T", null, "Q"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='content')]").exists());
    }

    // #32: POST title=101文字 → 400 + fieldErrors[title]
    @Test
    void createQuestion_titleTooLong_returns400WithFieldError() throws Exception {
        String longTitle = "a".repeat(101);
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest(longTitle, "C", "Q"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='title')]").exists());
    }

    // #33: PUT /api/questions/1 → 200 + id, title
    @Test
    void updateQuestion_valid_returns200() throws Exception {
        when(questionService.updateQuestion(eq(1L), any())).thenReturn(buildQuestionResponse(1L, "NewT", QuestionStatus.UNANSWERED));

        mockMvc.perform(put("/api/questions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("NewT", "C", "Q"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("NewT"));
    }

    // #34: PUT /api/questions/99 → 404
    @Test
    void updateQuestion_notExists_returns404() throws Exception {
        when(questionService.updateQuestion(eq(99L), any())).thenThrow(new ResourceNotFoundException("質問が見つかりません。id=99"));

        mockMvc.perform(put("/api/questions/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("T", "C", "Q"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // #35: DELETE /api/questions/1 → 200 + message
    @Test
    void deleteQuestion_exists_returns200() throws Exception {
        doNothing().when(questionService).deleteQuestion(1L);

        mockMvc.perform(delete("/api/questions/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("質問を削除しました。"));
    }

    // #36: DELETE /api/questions/99 → 404
    @Test
    void deleteQuestion_notExists_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("質問が見つかりません。id=99")).when(questionService).deleteQuestion(99L);

        mockMvc.perform(delete("/api/questions/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // #58: POST questioner=null → 400 + fieldErrors[questioner]
    @Test
    void createQuestion_missingQuestioner_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("T", "C", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='questioner')]").exists());
    }

    // #59: POST title="   " → 400 + fieldErrors[title]
    @Test
    void createQuestion_blankTitle_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("   ", "C", "Q"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='title')]").exists());
    }

    // #60: POST content="   " → 400 + fieldErrors[content]
    @Test
    void createQuestion_blankContent_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("T", "   ", "Q"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='content')]").exists());
    }

    // #61: PUT title="" → 400 + fieldErrors[title]
    @Test
    void updateQuestion_invalidInput_returns400WithFieldError() throws Exception {
        mockMvc.perform(put("/api/questions/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("", "C", "Q"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='title')]").exists());
    }
}
