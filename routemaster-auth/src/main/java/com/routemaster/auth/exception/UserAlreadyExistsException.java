package com.routemaster.auth.exception;

import org.springframework.http.HttpStatus;
import com.routemaster.common.exception.RouteMasterException;

public class UserAlreadyExistsException extends RouteMasterException {
    public UserAlreadyExistsException(String email) {
        super("User already exists with email" + email, HttpStatus.CONFLICT);
    }
}
