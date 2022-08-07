package com.jaewa.timesheet.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Log4j2
public class UnauthorizedExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    protected ResponseEntity<Object> handleUnauthorizedAccess(UnauthorizedException ex) {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, ex.getMessage());
        logger.error(ex.getMessage());
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }
}
