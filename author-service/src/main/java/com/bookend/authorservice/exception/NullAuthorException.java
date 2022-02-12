package com.bookend.authorservice.exception;

public class NullAuthorException extends Exception {
    public NullAuthorException() {
    }

    public NullAuthorException(String message) {
        super(message);
    }
}
