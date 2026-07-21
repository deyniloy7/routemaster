package com.routemaster.auth.exception;
import com.routemaster.common.exception.RouteMasterException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends RouteMasterException {

    public InvalidTokenException() {
        super("Invalid or expired token", HttpStatus.UNAUTHORIZED);
  ;  }
}
