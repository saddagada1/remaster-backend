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
public class RemasterResponse {
    @NotNull
    private UUID id;
    @NotNull
    private String url;
    @NotNull
    private String name;
    private String description;
    @NotNull
    private Float duration;
    @NotNull
    private Integer key;
    @NotNull
    private Integer mode;
    @NotNull
    private Float tempo;
    @NotNull
    private Integer timeSignature;
    @NotNull
    private Integer tuning;
    @NotNull
    private BasicUserResponse user;
    @NotNull
    private Date updatedAt;
    @NotNull
    private Date createdAt;
}
