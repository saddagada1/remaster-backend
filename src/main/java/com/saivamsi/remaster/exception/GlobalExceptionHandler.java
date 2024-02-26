package com.saivamsi.remaster.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Duration;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Integer requestLimit = 100;
    private static final Integer requestInterval = 60;

    @ModelAttribute
    public void rateLimit(HttpServletRequest request) {
        String identifier = request.getRemoteAddr();

        String value = redisTemplate.opsForValue().get(identifier);

        if (value != null) {
            int count = Integer.parseInt(value);
            if (count >= requestLimit) {
                throw new GlobalException(HttpStatus.TOO_MANY_REQUESTS, GlobalError.builder().subject("server").message("please wait while i catch my breath").build());
            } else {
                redisTemplate.opsForValue().increment(identifier);
            }
        } else {
            redisTemplate.opsForValue().set(identifier, "1", Duration.ofSeconds(requestInterval));
        }
    }
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
