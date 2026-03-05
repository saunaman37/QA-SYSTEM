package com.example.qabackend.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private OffsetDateTime timestamp;
    private List<FieldError> fieldErrors;

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = OffsetDateTime.now(java.time.ZoneId.of("Asia/Tokyo"));
        this.fieldErrors = new ArrayList<>();
    }

    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public List<FieldError> getFieldErrors() { return fieldErrors; }

    public void setFieldErrors(List<FieldError> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    public static class FieldError {
        private String field;
        private String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public String getMessage() { return message; }
    }
}
