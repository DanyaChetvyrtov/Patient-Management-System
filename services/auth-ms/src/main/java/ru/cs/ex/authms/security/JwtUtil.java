package ru.cs.ex.authms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateAccessToken(String email, String role) {
        Claims claims = Jwts.claims()
                .subject(email)
                .add("roles", role)
                .build();

        var validity = Instant.now()
                .plus(15, ChronoUnit.MINUTES);

        return Jwts.builder()
                .claims(claims)
                .expiration(Date.from(validity))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token)
                .getPayload().
                getExpiration().after(new Date());
    }
}
