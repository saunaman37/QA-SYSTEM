package com.example.qabackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AnswerRequest {

    @NotBlank(message = "回答内容は必須です。")
    @Size(max = 500, message = "回答内容は500文字以内で入力してください。")
    private String content;

    @NotBlank(message = "回答者名は必須です。")
    @Size(max = 50, message = "回答者名は50文字以内で入力してください。")
    private String responder;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getResponder() { return responder; }
    public void setResponder(String responder) { this.responder = responder; }
}
