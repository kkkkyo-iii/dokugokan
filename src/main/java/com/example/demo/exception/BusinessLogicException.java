package com.example.demo.exception;

public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message) {
        super(message);
    }
}