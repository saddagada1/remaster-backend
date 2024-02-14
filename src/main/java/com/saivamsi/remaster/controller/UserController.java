package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.request.UpdateUserRequest;
import com.saivamsi.remaster.response.AuthenticationResponse;
import com.saivamsi.remaster.response.PageResponse;
import com.saivamsi.remaster.response.UserResponse;
import com.saivamsi.remaster.service.PrincipleService;
import com.saivamsi.remaster.service.TokenService;
import com.saivamsi.remaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final PrincipleService principleService;
    private final UserService userService;
    private final TokenService tokenService;

    @GetMapping("/open/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity
                .ok(principleService.loadUserByUsername(username).getSafeUser());
    }

    @GetMapping("/open/search")
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @RequestParam String q) {
        return ResponseEntity
                .ok(userService.searchUsers(q, cursor, limit));
    }

    @PutMapping("/user")
    public ResponseEntity<AuthenticationResponse> updateUser(@RequestBody UpdateUserRequest request, @AuthenticationPrincipal ApplicationUser user) {
        boolean shouldRefresh = !user.getEmail().equals(request.getEmail()) || !user.getUsername().equals((request.getUsername()));
        UserResponse userUpdate = userService.updateUser(request, user);

        if (shouldRefresh) {
            String token = tokenService.generateToken(user);
            String refreshToken = tokenService.generateRefreshToken(user);
            Date expiresAt = tokenService.extractExpiration(token, "access_token");
            tokenService.revokeAllTokensForUser(user);
            tokenService.saveTokens(user, token, refreshToken);
            ResponseCookie refreshTokenCookie = tokenService.generateRefreshTokenCookie(refreshToken);
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(AuthenticationResponse.builder().user(userUpdate).accessToken(token).refreshToken(refreshToken).expiresAt(expiresAt).build());
        }

        return ResponseEntity
                .ok(AuthenticationResponse.builder().user(userUpdate).build());
    }
}
