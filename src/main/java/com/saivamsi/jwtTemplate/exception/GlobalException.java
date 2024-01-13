package com.saivamsi.jwtTemplate.exception;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class GlobalException extends RuntimeException {
    @NotNull
    private Error error;
    @NotNull
    private HttpStatus status;

    public GlobalException(HttpStatus status, Error error) {
        this.status = status;
        this.error = error;
    }
}
