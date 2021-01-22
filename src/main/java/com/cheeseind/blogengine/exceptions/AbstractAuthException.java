package com.cheeseind.blogengine.exceptions;

public abstract class AbstractAuthException extends RuntimeException {

    public AbstractAuthException(String message) {
        super(message);
    }

    public String prefix() {
        return "message";
    }
}
