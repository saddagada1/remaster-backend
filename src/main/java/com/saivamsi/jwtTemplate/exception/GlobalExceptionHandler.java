package com.saivamsi.jwtTemplate.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Object> handleGlobalException(GlobalException exception, WebRequest request) {
        return handleExceptionInternal(exception, exception.getError(),
                new HttpHeaders(), exception.getStatus(), request);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException exception, WebRequest request) {
        return handleExceptionInternal(exception, exception.getMessage(),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
