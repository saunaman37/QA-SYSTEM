package com.example.qabackend.controller;

import com.example.qabackend.dto.AnswerRequest;
import com.example.qabackend.dto.AnswerResponse;
import com.example.qabackend.entity.Answer;
import com.example.qabackend.entity.Question;
import com.example.qabackend.entity.QuestionStatus;
import com.example.qabackend.exception.BusinessException;
import com.example.qabackend.exception.GlobalExceptionHandler;
import com.example.qabackend.exception.ResourceNotFoundException;
import com.example.qabackend.service.AnswerService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AnswerControllerTest {

    @Mock AnswerService answerService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new AnswerController(answerService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
        objectMapper = new ObjectMapper();
    }

    private AnswerResponse buildAnswerResponse(Long id, Long questionId, String content) {
        Question q = new Question();
        q.setId(questionId);
        q.setTitle("Q");
        q.setContent("content");
        q.setQuestioner("questioner");
        q.setStatus(QuestionStatus.ANSWERED);
        q.setCreatedAt(LocalDateTime.now());
        q.setUpdatedAt(LocalDateTime.now());

        Answer a = new Answer();
        a.setId(id);
        a.setQuestion(q);
        a.setContent(content);
        a.setResponder("Resp");
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return new AnswerResponse(a);
    }

    private AnswerRequest buildRequest(String content, String responder) {
        AnswerRequest req = new AnswerRequest();
        req.setContent(content);
        req.setResponder(responder);
        return req;
    }

    private String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    // #37: POST /api/questions/1/answers → 201 + id, questionId, content
    @Test
    void createAnswer_valid_returns201() throws Exception {
        when(answerService.createAnswer(eq(1L), any())).thenReturn(buildAnswerResponse(10L, 1L, "Ans"));

        mockMvc.perform(post("/api/questions/1/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("Ans", "Resp"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.questionId").value(1))
            .andExpect(jsonPath("$.content").value("Ans"));
    }

    // #38: POST content="" → 400 + fieldErrors[content]
    @Test
    void createAnswer_missingContent_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions/1/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("", "Resp"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='content')]").exists());
    }

    // #39: POST 質問が存在しない → 404
    @Test
    void createAnswer_questionNotFound_returns404() throws Exception {
        when(answerService.createAnswer(eq(99L), any())).thenThrow(new ResourceNotFoundException("質問が見つかりません。id=99"));

        mockMvc.perform(post("/api/questions/99/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("Ans", "Resp"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // #40: POST 最大3件超過 → 400 + message
    @Test
    void createAnswer_maxAnswers_returns400() throws Exception {
        when(answerService.createAnswer(eq(1L), any())).thenThrow(new BusinessException("回答は最大3件までです。"));

        mockMvc.perform(post("/api/questions/1/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("Ans", "Resp"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("回答は最大3件までです。"));
    }

    // #41: PUT /api/answers/1 → 200 + id, content
    @Test
    void updateAnswer_valid_returns200() throws Exception {
        when(answerService.updateAnswer(eq(1L), any())).thenReturn(buildAnswerResponse(1L, 1L, "Updated"));

        mockMvc.perform(put("/api/answers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("Updated", "Resp"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.content").value("Updated"));
    }

    // #42: PUT /api/answers/99 → 404
    @Test
    void updateAnswer_answerNotFound_returns404() throws Exception {
        when(answerService.updateAnswer(eq(99L), any())).thenThrow(new ResourceNotFoundException("回答が見つかりません。id=99"));

        mockMvc.perform(put("/api/answers/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("Ans", "Resp"))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // #43: DELETE /api/answers/1 → 200 + message
    @Test
    void deleteAnswer_valid_returns200() throws Exception {
        doNothing().when(answerService).deleteAnswer(1L);

        mockMvc.perform(delete("/api/answers/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("回答を削除しました。"));
    }

    // #44: DELETE /api/answers/99 → 404
    @Test
    void deleteAnswer_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("回答が見つかりません。id=99")).when(answerService).deleteAnswer(99L);

        mockMvc.perform(delete("/api/answers/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    // #62: POST responder=null → 400 + fieldErrors[responder]
    @Test
    void createAnswer_missingResponder_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions/1/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("Ans", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='responder')]").exists());
    }

    // #63: POST content="   " → 400 + fieldErrors[content]
    @Test
    void createAnswer_blankContent_returns400WithFieldError() throws Exception {
        mockMvc.perform(post("/api/questions/1/answers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("   ", "Resp"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='content')]").exists());
    }

    // #64: PUT content="" → 400 + fieldErrors[content]
    @Test
    void updateAnswer_invalidInput_returns400WithFieldError() throws Exception {
        mockMvc.perform(put("/api/answers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(buildRequest("", "Resp"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors[?(@.field=='content')]").exists());
    }
}
