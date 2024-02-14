package com.saivamsi.remaster.controller;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.request.LoginRequest;
import com.saivamsi.remaster.request.RegisterRequest;
import com.saivamsi.remaster.response.AuthenticationResponse;
import com.saivamsi.remaster.service.AuthenticationService;
import com.saivamsi.remaster.service.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody RegisterRequest request) {
        AuthenticationResponse auth = authenticationService.registerUser(request.getUsername(), request.getEmail(), request.getPassword());
        ResponseCookie refreshToken = tokenService.generateRefreshTokenCookie(auth.getRefreshToken());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshToken.toString())
                .body(auth);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody LoginRequest request) {
        AuthenticationResponse auth = authenticationService.loginUser(request.getPrincipal(), request.getPassword());
        ResponseCookie refreshToken = tokenService.generateRefreshTokenCookie(auth.getRefreshToken());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, refreshToken.toString())
                .body(auth);
    }

    @PostMapping("/refresh_token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        GlobalException noRefreshToken = new GlobalException(HttpStatus.BAD_REQUEST, GlobalError.builder().subject("cookies").message("no refresh token").build());
        Cookie[] cookies = request.getCookies();
        if (cookies == null) throw noRefreshToken;
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> tokenService.getRefreshTokenCookieName().equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst().orElseThrow(() -> noRefreshToken);
        AuthenticationResponse auth = authenticationService.refreshToken(refreshToken);
        ResponseCookie newRefreshToken = tokenService.generateRefreshTokenCookie(auth.getRefreshToken());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, newRefreshToken.toString())
                .body(auth);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
        authenticationService.logoutUser(request, response);
        return ResponseEntity
                .ok("success");
    }
}
