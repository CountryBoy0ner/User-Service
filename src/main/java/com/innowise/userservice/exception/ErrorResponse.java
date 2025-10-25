package com.innowise.userservice.exception;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
@Data
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime timestamp = OffsetDateTime.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;


    public ErrorResponse(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = validationErrors;
    }

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(status, error, message, path, fieldErrors);
    }
}