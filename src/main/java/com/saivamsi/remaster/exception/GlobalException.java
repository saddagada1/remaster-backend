package com.saivamsi.remaster.exception;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class GlobalException extends RuntimeException {
    @NotNull
    private GlobalError error;
    @NotNull
    private HttpStatus status;

    public GlobalException(HttpStatus status, GlobalError error) {
        this.status = status;
        this.error = error;
    }
}
