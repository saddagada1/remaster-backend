package com.saivamsi.remaster.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
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
    private String loops;
    @NotNull
    private BasicUserResponse user;
    private Boolean likedBySessionUser;
    @NotNull
    private Integer totalLikes;
    @NotNull
    private Integer totalPlays;
    @NotNull
    private Date updatedAt;
    @NotNull
    private Date createdAt;
}
