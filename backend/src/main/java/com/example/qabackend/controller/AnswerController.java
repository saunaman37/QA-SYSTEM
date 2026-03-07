package com.example.qabackend.controller;

import com.example.qabackend.dto.AnswerRequest;
import com.example.qabackend.dto.AnswerResponse;
import com.example.qabackend.service.AnswerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AnswerController {

    private static final Logger log = LoggerFactory.getLogger(AnswerController.class);

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/api/questions/{id}/answers")
    public ResponseEntity<AnswerResponse> createAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerRequest req) {
        AnswerResponse response = answerService.createAnswer(id, req);
        log.info("[ANSWER_CREATE] id={} questionId={}", response.getId(), response.getQuestionId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/api/answers/{answerId}")
    public ResponseEntity<AnswerResponse> updateAnswer(
            @PathVariable Long answerId,
            @Valid @RequestBody AnswerRequest req) {
        AnswerResponse response = answerService.updateAnswer(answerId, req);
        log.info("[ANSWER_UPDATE] id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/answers/{answerId}")
    public ResponseEntity<Map<String, String>> deleteAnswer(@PathVariable Long answerId) {
        answerService.deleteAnswer(answerId);
        log.info("[ANSWER_DELETE] id={}", answerId);
        return ResponseEntity.ok(Map.of("message", "回答を削除しました。"));
    }
}
