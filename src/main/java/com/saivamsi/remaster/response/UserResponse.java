package com.saivamsi.remaster.response;

import com.saivamsi.remaster.model.Role;
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
public class UserResponse {
    @NotNull
    private UUID id;
    @NotNull
    private String username;
    @NotNull
    private String email;
    private String name;
    private String bio;
    private String image;
    @NotNull
    private Role role;
    @NotNull
    private boolean verified;
    @NotNull
    private Integer totalRemasters;
    @NotNull
    private Integer totalFollowers;
    @NotNull
    private Integer totalFollowing;
    private Boolean followedBySessionUser;
}
