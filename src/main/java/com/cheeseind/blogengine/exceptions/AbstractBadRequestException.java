package com.cheeseind.blogengine.exceptions;

public abstract class AbstractBadRequestException extends RuntimeException {

    public AbstractBadRequestException() {
    }

    public AbstractBadRequestException(String message) {
        super(message);
    }
}
