package com.saivamsi.jwtTemplate.exception;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Error {
    @NotNull
    private String subject;
    @NotNull
    private String message;
}
