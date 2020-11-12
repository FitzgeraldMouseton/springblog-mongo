package com.cheeseind.blogengine.exceptions;

public class UnauthenticatedUserException extends RuntimeException {

    public UnauthenticatedUserException() {
    }

    public UnauthenticatedUserException(String message, Throwable cause) {
        super(message);
    }
}
