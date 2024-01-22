package com.saivamsi.remaster.service;

import com.saivamsi.remaster.model.ApplicationUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
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
}
