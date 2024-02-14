package com.saivamsi.remaster.service;

import com.saivamsi.remaster.model.ApplicationUser;
import com.saivamsi.remaster.model.Token;
import com.saivamsi.remaster.repository.TokenRepository;
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
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class TokenService {

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
    private final TokenRepository tokenRepository;

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

    public String generateToken(ApplicationUser user) {
        return generateToken(new HashMap<>(), user);
    }

    public String generateToken(Map<String, Object> extraClaims, ApplicationUser user) {
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

    public boolean isTokenValid(String token, ApplicationUser user, String tokenType) {
        final String subject = extractSubject(token, tokenType);
        return ((subject.equals(user.getUsername()) || subject.equals(user.getEmail())) && !isTokenExpired(token, tokenType));
    }

    public boolean isTokenExpired(String token, String tokenType) {
        return extractExpiration(token, tokenType).before(new Date());
    }

    public Date extractExpiration(String token, String tokenType) {
        return extractClaim(token, Claims::getExpiration, tokenType);
    }

    public void saveTokens(ApplicationUser user, String token, String refreshToken) {
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

    public void revokeAllTokensForUser(ApplicationUser user) {
        List<Token> tokens = tokenRepository.findAllValidTokensByUser(user.getId());

        if (tokens.isEmpty()) return;

        tokens.forEach(t -> {
            t.setRevoked(true);
            t.setExpired(true);
        });

        tokenRepository.saveAll(tokens);
    }

    public void revokeTokenForUser(String token) {
        Token savedToken = tokenRepository.findByToken(token).orElse(null);

        if (savedToken != null) {
            savedToken.setExpired(true);
            savedToken.setRevoked(true);
            tokenRepository.save(savedToken);
        }
    }

    public boolean isTokenInUse(String token) {
        return tokenRepository.findByToken(token).map(t -> !t.isExpired() && !t.isRevoked()).orElse(false);
    }
}
