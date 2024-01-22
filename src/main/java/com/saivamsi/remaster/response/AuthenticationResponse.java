package com.saivamsi.remaster.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    @NotNull
    private UserResponse user;
    @NotNull
    private String accessToken;
    @NotNull
    private String refreshToken;
    @NotNull
    private Date expiresAt;
}
