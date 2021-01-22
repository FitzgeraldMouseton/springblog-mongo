package com.cheeseind.blogengine.exceptions;

public class AbstractUnauthenticatedException extends RuntimeException {

    public AbstractUnauthenticatedException() {
    }

    public AbstractUnauthenticatedException(String message) {
        super(message);
    }

    public String prefix() {
        return "message";
    }
}
