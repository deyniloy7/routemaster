package com.routemaster.auth.exception;
import com.routemaster.common.exception.RouteMasterException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends RouteMasterException {
    public UserNotFoundException(String email) {
        super("User not found with email: " + email, HttpStatus.NOT_FOUND);
    }
}
