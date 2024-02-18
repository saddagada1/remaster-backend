package com.saivamsi.remaster.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BasicRemasterResponse {
    @NotNull
    private UUID id;
    @NotNull
    private String url;
    @NotNull
    private String name;
    @NotNull
    private BasicUserResponse user;
    @NotNull
    private Date createdAt;
}
