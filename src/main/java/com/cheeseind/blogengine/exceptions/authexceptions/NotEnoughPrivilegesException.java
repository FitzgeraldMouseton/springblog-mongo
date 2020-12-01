package com.cheeseind.blogengine.exceptions.authexceptions;

import com.cheeseind.blogengine.exceptions.AbstractBadRequestException;

public class NotEnoughPrivilegesException extends AbstractBadRequestException {

    public NotEnoughPrivilegesException(final String message) {
        super(message);
    }
}
