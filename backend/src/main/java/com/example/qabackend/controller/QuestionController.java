package com.example.qabackend.controller;

import com.example.qabackend.dto.QuestionDetailResponse;
import com.example.qabackend.dto.QuestionRequest;
import com.example.qabackend.dto.QuestionResponse;
import com.example.qabackend.service.QuestionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private static final Logger log = LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(questionService.getQuestions(keyword, status, sort));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDetailResponse> getQuestion(@PathVariable Long id) {
        return ResponseEntity.ok(questionService.getQuestion(id));
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest req) {
        QuestionResponse response = questionService.createQuestion(req);
        log.info("[QUESTION_CREATE] id={} title={}", response.getId(), response.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponse> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest req) {
        QuestionResponse response = questionService.updateQuestion(id, req);
        log.info("[QUESTION_UPDATE] id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        log.info("[QUESTION_DELETE] id={}", id);
        return ResponseEntity.ok(Map.of("message", "質問を削除しました。"));
    }
}
