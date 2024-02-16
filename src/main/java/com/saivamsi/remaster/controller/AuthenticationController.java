package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.request.LoginRequest;
import com.saivamsi.remaster.request.RegisterRequest;
import com.saivamsi.remaster.response.AuthenticationResponse;
import com.saivamsi.remaster.service.AuthenticationService;
import com.saivamsi.remaster.service.SessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final SessionService sessionService;

    @PostMapping("/auth/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterRequest request) {
        AuthenticationResponse auth = authenticationService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
        ResponseCookie refreshTokenCookie = sessionService.generateRefreshTokenCookie(auth.getRefreshToken());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(auth);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody LoginRequest request) {
        AuthenticationResponse auth = authenticationService.loginUser(request.getPrincipal(), request.getPassword());
        ResponseCookie refreshTokenCookie = sessionService.generateRefreshTokenCookie(auth.getRefreshToken());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(auth);
    }

    @PostMapping("/auth/refresh_token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        GlobalException noRefreshToken = new GlobalException(HttpStatus.BAD_REQUEST, GlobalError.builder().subject("cookies").message("no refresh token").build());
        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw noRefreshToken;
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> sessionService.getRefreshTokenCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst().orElseThrow(() -> noRefreshToken);
        AuthenticationResponse auth = authenticationService.refreshToken(refreshToken);
        ResponseCookie refreshTokenCookie = sessionService.generateRefreshTokenCookie(auth.getRefreshToken());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(auth);
    }

    @PostMapping("/user/send-verification-email")
    public ResponseEntity<String> sendVerificationEmail(@AuthenticationPrincipal ApplicationUser user) {
        authenticationService.sendVerificationEmail(user);
        return ResponseEntity
                .ok("success");
    }

    @PostMapping("/auth/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authenticationService.verifyEmail(token);
        return ResponseEntity
                .ok("success");
    }

    @PostMapping("/auth/send-forgot-password-email")
    public ResponseEntity<String> sendForgotPasswordEmail(@RequestParam String email) {
        authenticationService.sendForgotPasswordEmail(email);
        return ResponseEntity
                .ok("success");
    }

    @PostMapping("/auth/change-forgotten-password")
    public ResponseEntity<AuthenticationResponse> changeForgottenPassword(@RequestParam  String token, @RequestParam String password) {
        AuthenticationResponse auth = authenticationService.changeForgottenPassword(token, password);
        ResponseCookie refreshTokenCookie = sessionService.generateRefreshTokenCookie(auth.getRefreshToken());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(auth);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logoutUser(request, response);
        return ResponseEntity
                .ok("success");
    }
}
