package com.innowise.userservice.exception.type;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}