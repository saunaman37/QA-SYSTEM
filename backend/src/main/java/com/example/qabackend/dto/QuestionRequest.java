package com.example.qabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class QuestionRequest {

    @NotBlank(message = "タイトルは必須です。")
    @Size(max = 100, message = "タイトルは100文字以内で入力してください。")
    private String title;

    @NotBlank(message = "質問内容は必須です。")
    @Size(max = 1000, message = "質問内容は1000文字以内で入力してください。")
    private String content;

    @NotBlank(message = "質問者名は必須です。")
    @Size(max = 50, message = "質問者名は50文字以内で入力してください。")
    private String questioner;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getQuestioner() { return questioner; }
    public void setQuestioner(String questioner) { this.questioner = questioner; }
}
