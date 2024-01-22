package com.saivamsi.remaster.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicUserResponse {
    @NotNull
    private UUID id;
    @NotNull
    private String username;
}
