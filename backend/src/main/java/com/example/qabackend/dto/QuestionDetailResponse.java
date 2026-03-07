package com.example.qabackend.dto;

import com.example.qabackend.entity.Question;
import com.example.qabackend.entity.QuestionStatus;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionDetailResponse {

    private Long id;
    private String title;
    private String content;
    private String questioner;
    private QuestionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AnswerResponse> answers;

    public QuestionDetailResponse(Question q, List<AnswerResponse> answers) {
        this.id = q.getId();
        this.title = q.getTitle();
        this.content = q.getContent();
        this.questioner = q.getQuestioner();
        this.status = q.getStatus();
        this.createdAt = q.getCreatedAt();
        this.updatedAt = q.getUpdatedAt();
        this.answers = answers;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getQuestioner() { return questioner; }
    public QuestionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<AnswerResponse> getAnswers() { return answers; }
}
