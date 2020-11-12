package com.cheeseind.blogengine.exceptions;

public class NotEnoughPrivilegesException extends AbstractBadRequestException {

    public NotEnoughPrivilegesException(final String message) {
        super(message);
    }
}
