package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Session;
import com.saivamsi.remaster.request.UpdateUserRequest;
import com.saivamsi.remaster.response.AuthenticationResponse;
import com.saivamsi.remaster.response.PageResponse;
import com.saivamsi.remaster.response.UserResponse;
import com.saivamsi.remaster.service.PrincipleService;
import com.saivamsi.remaster.service.SessionService;
import com.saivamsi.remaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final PrincipleService principleService;
    private final UserService userService;
    private final SessionService sessionService;

    @GetMapping("/open/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return ResponseEntity
                .ok(userService.getUserByUsername(username).getSafeUser());
    }

    @GetMapping("/open/search")
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @RequestParam String q) {
        return ResponseEntity
                .ok(userService.searchUsers(q, cursor, limit));
    }

    @PutMapping(value = "/user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthenticationResponse> updateUser(@RequestPart() UpdateUserRequest request,
                                                             @RequestPart(required = false) MultipartFile imageFile,
                                                             @AuthenticationPrincipal ApplicationUser user) {
        if (imageFile != null) {
            String imageUrl = userService.uploadProfilePicture(imageFile, user);
            request.setImage(imageUrl);
        }

        if (request.getImage() == null && user.getImage() != null) {
            System.out.println("test");
            userService.deleteProfilePicture(user);
        }

        boolean shouldRefresh = !user.getEmail().equals(request.getEmail()) || !user.getUsername().equals((request.getUsername()));
        UserResponse userUpdate = userService.updateUser(request, user);

        if (shouldRefresh) {
            Session session = sessionService.createSession(user);
            ResponseCookie refreshTokenCookie = sessionService.generateRefreshTokenCookie(session.getRefreshToken());
            return ResponseEntity
                    .ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(AuthenticationResponse.builder()
                            .user(userUpdate)
                            .accessToken(session.getAccessToken())
                            .refreshToken(session.getRefreshToken())
                            .expiresAt(session.getAccessTokenExpiresAt())
                            .build());
        }

        return ResponseEntity
                .ok(AuthenticationResponse.builder().user(userUpdate).build());
    }
}
