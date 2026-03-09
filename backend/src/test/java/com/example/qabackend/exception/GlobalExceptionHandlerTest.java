package com.example.qabackend.exception;

import com.example.qabackend.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock MethodArgumentNotValidException mockValidationEx;
    @Mock BindingResult bindingResult;

    // #45: ResourceNotFoundException → 404
    @Test
    void handleResourceNotFoundException_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("質問が見つかりません。id=99");

        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("質問が見つかりません。id=99");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // #46: BusinessException → 400
    @Test
    void handleBusinessException_returns400() {
        BusinessException ex = new BusinessException("回答は最大3件までです。");

        ResponseEntity<ErrorResponse> response = handler.handleBusiness(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("回答は最大3件までです。");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    // #47: MethodArgumentNotValidException → 400 + fieldErrors
    @Test
    void handleValidationException_returns400WithFieldErrors() {
        FieldError fieldError = new FieldError("questionRequest", "title", "タイトルは必須です。");
        when(mockValidationEx.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(mockValidationEx);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("入力内容に誤りがあります。");
        assertThat(response.getBody().getFieldErrors()).hasSize(1);
        assertThat(response.getBody().getFieldErrors().get(0).getField()).isEqualTo("title");
        assertThat(response.getBody().getFieldErrors().get(0).getMessage()).isEqualTo("タイトルは必須です。");
    }

    // #48: 予期しない例外 → 500 + fieldErrors は空リスト
    @Test
    void handleUnexpectedException_returns500() {
        RuntimeException ex = new RuntimeException("予期しないエラー");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("予期しないエラーが発生しました。");
        assertThat(response.getBody().getFieldErrors()).isEmpty();
    }
}
