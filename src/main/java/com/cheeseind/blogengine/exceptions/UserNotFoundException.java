package com.cheeseind.blogengine.exceptions;

public class UserNotFoundException extends AbstractAuthException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
