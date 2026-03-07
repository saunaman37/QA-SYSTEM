package com.example.qabackend.dto;

import com.example.qabackend.entity.Answer;

import java.time.LocalDateTime;

public class AnswerResponse {

    private Long id;
    private Long questionId;
    private String content;
    private String responder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AnswerResponse(Answer a) {
        this.id = a.getId();
        this.questionId = a.getQuestion().getId();
        this.content = a.getContent();
        this.responder = a.getResponder();
        this.createdAt = a.getCreatedAt();
        this.updatedAt = a.getUpdatedAt();
    }

    public Long getId() { return id; }
    public Long getQuestionId() { return questionId; }
    public String getContent() { return content; }
    public String getResponder() { return responder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
