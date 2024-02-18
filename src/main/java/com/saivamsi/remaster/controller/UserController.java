package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Session;
import com.saivamsi.remaster.request.UpdateUserRequest;
import com.saivamsi.remaster.response.*;
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

    private final UserService userService;
    private final SessionService sessionService;

    @GetMapping("/open/search/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username, @RequestParam(required = false) UUID userId) {
        return ResponseEntity
                .ok(userService.getUserByUsername(username, userId));
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

    @PostMapping("/user/follow")
    public ResponseEntity<String> followUser(@RequestParam UUID id, @AuthenticationPrincipal ApplicationUser user) {
        userService.follow(id, user);
        return ResponseEntity.ok("success");
    }

    @PostMapping("/user/unfollow")
    public ResponseEntity<String> unfollowUser(@RequestParam UUID id, @AuthenticationPrincipal ApplicationUser user) {
        userService.unfollow(id, user);
        return ResponseEntity.ok("success");
    }

    @GetMapping("/user/followers")
    public ResponseEntity<PageResponse<BasicUserResponse>> getUserFollowers(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity
                .ok(userService.getUserFollowers(user, cursor, limit));
    }

    @GetMapping("/user/following")
    public ResponseEntity<PageResponse<BasicUserResponse>> getUserFollowing(@RequestParam(required = false) UUID cursor, @RequestParam Integer limit, @AuthenticationPrincipal ApplicationUser user) {
        return ResponseEntity
                .ok(userService.getUserFollowing(user, cursor, limit));
    }
}
