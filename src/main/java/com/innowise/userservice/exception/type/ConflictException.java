package com.innowise.userservice.exception.type;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}