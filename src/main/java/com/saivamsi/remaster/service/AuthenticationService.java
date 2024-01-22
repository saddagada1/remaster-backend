package com.saivamsi.remaster.service;

import com.saivamsi.remaster.exception.GlobalError;
import com.saivamsi.remaster.exception.GlobalException;
import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Token;
import com.saivamsi.remaster.repository.TokenRepository;
import com.saivamsi.remaster.response.AuthenticationResponse;
import com.saivamsi.remaster.model.Role;
import com.saivamsi.remaster.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    private void saveTokens(ApplicationUser user, String token, String refreshToken) {
        tokenRepository.saveAll(List.of(Token.builder()
                .user(user)
                .token(token)
                .type("access_token")
                .build(), Token.builder()
                .user(user)
                .token(refreshToken)
                .type("access_token")
                .build()));
    }

    private void revokeAllTokensForUser(ApplicationUser user) {
        List<Token> tokens = tokenRepository.findAllValidTokensByUser(user.getId());

        if (tokens.isEmpty()) return;

        tokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });

        tokenRepository.saveAll(tokens);
    }

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
                .build());
        String token = tokenService.generateToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);
        Date expiresAt = tokenService.extractExpiration(token, "access_token");
        saveTokens(user, token, refreshToken);
        return AuthenticationResponse.builder()
                .user(user.getSafeUser())
                .accessToken(token)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();
    }

    public AuthenticationResponse loginUser(String principal, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(principal, password));
            ApplicationUser user = userRepository.findByUsernameOrEmail(principal, principal).orElseThrow();
            String token = tokenService.generateToken(user);
            String refreshToken = tokenService.generateRefreshToken(user);
            Date expiresAt = tokenService.extractExpiration(token, "access_token");
            revokeAllTokensForUser(user);
            saveTokens(user, token, refreshToken);
            return AuthenticationResponse.builder()
                    .user(user.getSafeUser())
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .expiresAt(expiresAt)
                    .build();
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.FORBIDDEN, GlobalError.builder().subject("emailOrUsername").message("invalid login").build());
        }
    }

    public AuthenticationResponse refreshToken(String refreshToken) {
        try {
            final String userPrinciple = tokenService.extractSubject(refreshToken, "refresh_token");
            if (userPrinciple != null) {
                ApplicationUser user = userRepository.findByUsernameOrEmail(userPrinciple, userPrinciple).orElseThrow();
                boolean isTokenInUse = tokenRepository.findByToken(refreshToken).map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
                if (tokenService.isTokenValid(refreshToken, user, "refresh_token") && isTokenInUse) {
                    String token = tokenService.generateToken(user);
                    String newRefreshToken = tokenService.generateRefreshToken(user);
                    Date expiresAt = tokenService.extractExpiration(token, "access_token");
                    revokeAllTokensForUser(user);
                    saveTokens(user, token, newRefreshToken);
                    return AuthenticationResponse.builder()
                            .user(user.getSafeUser())
                            .accessToken(token)
                            .refreshToken(newRefreshToken)
                            .expiresAt(expiresAt)
                            .build();
                }
            }
        } catch (Exception e) {
            throw new GlobalException(HttpStatus.FORBIDDEN, GlobalError.builder().subject("account").message("unable to refresh token").build());
        }

        throw new GlobalException(HttpStatus.FORBIDDEN, GlobalError.builder().subject("account").message("unable to refresh token").build());
    }

    public void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        Cookie deleteRefreshToken = new Cookie(tokenService.getRefreshTokenCookieName(), null);
        deleteRefreshToken.setMaxAge(0);
        response.addCookie(deleteRefreshToken);

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }

        final String token = authHeader.split(" ", 2)[1];

        Token savedToken = tokenRepository.findByToken(token).orElse(null);

        if (savedToken != null) {
            savedToken.setExpired(true);
            savedToken.setRevoked(true);
            tokenRepository.save(savedToken);
        }

        SecurityContextHolder.clearContext();
    }
}
