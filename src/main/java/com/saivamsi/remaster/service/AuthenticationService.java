package com.saivamsi.remaster.service;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Session;
import com.saivamsi.remaster.response.AuthenticationResponse;
import com.saivamsi.remaster.model.Role;
import com.saivamsi.remaster.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SessionService sessionService;
    private final RedisTemplate<String, String> redisTemplate;
    private final ResendService resendService;
    private final String verifyEmailPrefix = "verify_email";
    private final String forgotPasswordPrefix = "forgot_password";

    public AuthenticationResponse registerUser(String username, String email, String password) {
        Optional<ApplicationUser> existingUser = userRepository.findByUsernameOrEmail(username.toLowerCase(), email.toLowerCase());

        if (existingUser.isPresent()) {
            if (existingUser.get().getEmail().equals(email.toLowerCase())) {
                throw new GlobalException(HttpStatus.CONFLICT, GlobalError.builder().subject("email").message("email in use").build());
            }
            if (existingUser.get().getUsername().equals(username.toLowerCase())) {
                throw new GlobalException(HttpStatus.CONFLICT, GlobalError.builder().subject("username").message("username in use").build());
            }
        }

        ApplicationUser user = userRepository.save(ApplicationUser.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .totalRemasters(0)
                .totalFollowers(0)
                .totalFollowing(0)
                .build());


        Session session = sessionService.createSession(user);

//        String verifyEmailToken = UUID.randomUUID().toString();
//        redisTemplate.opsForValue().set(verifyEmailPrefix + verifyEmailToken, user.getId().toString(), Duration.ofDays(1));
//        resendService.sendVerificationEmail(user.getEmail(), "Welcome Aboard!", verifyEmailToken);

        return AuthenticationResponse.builder()
                .user(user.getSafeUser())
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .expiresAt(session.getAccessTokenExpiresAt())
                .build();
    }

    public AuthenticationResponse loginUser(String principal, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(principal, password));
            ApplicationUser user = userRepository.findByUsernameOrEmail(principal, principal).orElseThrow();

            Session session = sessionService.createSession(user);

            return AuthenticationResponse.builder()
                    .user(user.getSafeUser())
                    .accessToken(session.getAccessToken())
                    .refreshToken(session.getRefreshToken())
                    .expiresAt(session.getAccessTokenExpiresAt())
                    .build();
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.FORBIDDEN, GlobalError.builder().subject("emailOrUsername").message("invalid login").build());
        }
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        GlobalException unableToRefresh = new GlobalException(HttpStatus.FORBIDDEN, GlobalError.builder().subject("account").message("unable to refresh token").build());
        try {
            final String userPrinciple = sessionService.extractSubject(refreshToken, "refresh_token");
            if (userPrinciple == null) {
                throw unableToRefresh;
            }

            ApplicationUser user = userRepository.findByUsernameOrEmail(userPrinciple, userPrinciple).orElseThrow();

            if (!sessionService.isSessionValid(refreshToken, user, "refresh_token")) {
                throw unableToRefresh;
            }

            Session session = sessionService.refreshSession(refreshToken, user);

            return AuthenticationResponse.builder()
                    .user(user.getSafeUser())
                    .accessToken(session.getAccessToken())
                    .refreshToken(session.getRefreshToken())
                    .expiresAt(session.getAccessTokenExpiresAt())
                    .build();

        } catch (Exception e) {
            throw unableToRefresh;
        }
    }

    public void sendVerificationEmail(ApplicationUser user) {
        if (user.isVerified()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, GlobalError.builder().subject("user").message("user already verified").build());
        }
        String verifyEmailToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(verifyEmailPrefix + verifyEmailToken, user.getId().toString(), Duration.ofDays(1));
        resendService.sendVerificationEmail(user.getEmail(), "Verify Email!", verifyEmailToken);
    }

    public void verifyEmail(String token) {
        String userId = redisTemplate.opsForValue().getAndDelete(verifyEmailPrefix + token);

        if (userId == null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("token").message("token expired").build());
        }

        ApplicationUser user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build()));

        if (user.isVerified()) {
            throw new GlobalException(HttpStatus.BAD_REQUEST, GlobalError.builder().subject("user").message("user already verified").build());
        }

        user.setVerified(true);

        userRepository.save(user);
    }

    public void sendForgotPasswordEmail(String email) {
        ApplicationUser user = userRepository.findByEmail(email).orElseThrow(
                () -> new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build()));

        String forgotPasswordToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(forgotPasswordPrefix + forgotPasswordToken, user.getId().toString(), Duration.ofDays(1));
        resendService.sendForgotPasswordEmail(user.getEmail(), forgotPasswordToken);
    }

    public AuthenticationResponse changeForgottenPassword(String token, String password) {
        String userId = redisTemplate.opsForValue().getAndDelete(forgotPasswordPrefix + token);

        if (userId == null) {
            throw new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("token").message("token expired").build());
        }

        ApplicationUser user = userRepository.findById(UUID.fromString(userId)).orElseThrow(
                () -> new GlobalException(HttpStatus.NOT_FOUND, GlobalError.builder().subject("user").message("user not found").build()));

        user.setPassword(passwordEncoder.encode(password));
        user = userRepository.save(user);

        sessionService.revokeAllSessionsForUser(user);

        Session session = sessionService.createSession(user);
        return AuthenticationResponse.builder()
                .user(user.getSafeUser())
                .accessToken(session.getAccessToken())
                .refreshToken(session.getRefreshToken())
                .expiresAt(session.getAccessTokenExpiresAt())
                .build();
    }

    public void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        Cookie deleteRefreshToken = new Cookie(sessionService.getRefreshTokenCookieName(), null);
        deleteRefreshToken.setMaxAge(0);
        response.addCookie(deleteRefreshToken);

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        final String token = authHeader.split(" ", 2)[1];

        sessionService.revokeSessionForUser(token, "access_token");

        SecurityContextHolder.clearContext();
    }
}
