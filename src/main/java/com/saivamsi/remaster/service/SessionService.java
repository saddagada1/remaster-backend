package com.saivamsi.remaster.service;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Session;
import com.saivamsi.remaster.repository.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class SessionService {

    @Value("${ACCESS_TOKEN_SECRET}")
    private String accessTokenSecret;
    @Value("${REFRESH_TOKEN_SECRET}")
    private String refreshTokenSecret;
    @Value("${ACCESS_TOKEN_EXPIRES}")
    private Long accessTokenExpires;
    @Value("${REFRESH_TOKEN_EXPIRES}")
    private Long refreshTokenExpires;
    @Value("${REFRESH_TOKEN_COOKIE_NAME}")
    private String refreshTokenCookieName;
    private final SessionRepository sessionRepository;

    public String extractSubject(String token, String tokenType) {
        return extractClaim(token, Claims::getSubject, tokenType);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, String tokenType) {
        final Claims claims = extractClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    public Claims extractClaims(String token, String tokenType) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey(tokenType))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey(String tokenType) {
        byte[] keyBytes = Decoders.BASE64.decode(tokenType.equals("access_token") ? accessTokenSecret : refreshTokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(ApplicationUser user) {
        return generateAccessToken(new HashMap<>(), user);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, ApplicationUser user) {
        return buildToken(extraClaims, user, accessTokenExpires, "access_token");
    }

    public String generateRefreshToken(ApplicationUser user) {
        return buildToken(new HashMap<>(), user, refreshTokenExpires, "refresh_token");
    }

    public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(this.refreshTokenCookieName, refreshToken).maxAge(this.refreshTokenExpires / 1000)
                .secure(true)
                .httpOnly(true)
                .path("/auth")
                .sameSite("lax").build();
    }

    public String buildToken(Map<String, Object> extraClaims, ApplicationUser user, Long expiration, String tokenType) {
        return Jwts.builder()
                .addClaims(extraClaims)
                .setSubject(user.getUsername())
                .setId(user.getId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(tokenType), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isSessionValid(String token, ApplicationUser user, String tokenType) {
        final String subject = extractSubject(token, tokenType);
        Boolean sessionExists;
        if (tokenType.equals("access_token")) {
            sessionExists = sessionRepository.existsByAccessToken(token);
        } else {
            sessionExists = sessionRepository.existsByRefreshToken(token);
        }
        return ((subject.equals(user.getUsername()) || subject.equals(user.getEmail())) && !isTokenExpired(token, tokenType) && sessionExists);
    }

    public boolean isTokenExpired(String token, String tokenType) {
        return extractExpiration(token, tokenType).before(new Date());
    }

    public Date extractExpiration(String token, String tokenType) {
        return extractClaim(token, Claims::getExpiration, tokenType);
    }

    public void revokeAllSessionsForUser(ApplicationUser user) {
        sessionRepository.deleteAllSessionsByUser(user.getId());
    }

    public void revokeSessionForUser(String token, String tokenType) {
        if (tokenType.equals("access_token")) {
            sessionRepository.deleteByAccessToken(token);
        } else {
            sessionRepository.deleteByRefreshToken(token);
        }
    }

    public Session createSession(ApplicationUser user) {
        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(user);
        Date accessTokenExpiresAt = extractExpiration(accessToken, "access_token");
        Date refreshTokenExpiresAt = extractExpiration(refreshToken, "refresh_token");

        return sessionRepository.save(Session.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenExpiresAt)
                .refreshTokenExpiresAt(refreshTokenExpiresAt)
                .build());
    }

    public Session refreshSession(String refreshToken, ApplicationUser user) {
        sessionRepository.deleteByRefreshToken(refreshToken);
        return createSession(user);
    }
}
