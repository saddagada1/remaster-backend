package com.saivamsi.jwtTemplate.response;

import com.saivamsi.jwtTemplate.model.Role;
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
    private Date updatedAt;
    @NotNull
    private Date createdAt;
}
