package com.saivamsi.remaster.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String image;
    @NotNull
    private String username;
    @NotNull
    private String email;
    private String name;
    private String bio;
    private String password;
}
