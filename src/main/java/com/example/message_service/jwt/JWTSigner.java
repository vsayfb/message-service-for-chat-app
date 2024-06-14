package com.example.message_service.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.message_service.jwt.claims.JWTClaims;
import com.example.message_service.jwt.claims.JWTPayload;

@Component
public class JWTSigner {

    private Long expireAfterMS;

    private SecretKey secretKey;

    public JWTSigner(@Value("${jwt_base64url_encoded_secret_key}") String base64URLencodedString) {
        setSecretKey(base64URLencodedString);
    }

    private final static long expireAfterFiveMinutes = 300000;

    public String sign(JWTPayload payload) throws IllegalArgumentException {

        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null or empty.");
        }

        Map<String, Object> claims = new HashMap<>();

        claims.put("sub", payload.getUserId());
        claims.put("username", payload.getUsername());
        claims.put("roomId", payload.getRoomId());

        return Jwts.builder()
                .signWith(secretKey)
                .claims(claims)
                .issuer("http://message-server")
                .expiration(expireAfterMS == null ? new Date(System.currentTimeMillis() + expireAfterFiveMinutes)
                        : new Date(System.currentTimeMillis() + expireAfterMS))
                .issuedAt(new Date())
                .id(UUID.randomUUID().toString())
                .compact();
    }

    public Optional<JWTClaims> validateToken(String jwt) throws IllegalArgumentException {

        if (jwt == null || jwt.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty.");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            JWTClaims jwtClaims = new JWTClaims();

            jwtClaims.setUsername((String) claims.get("username"));
            jwtClaims.setRoomId((String) claims.get("roomId"));
            jwtClaims.setUserId((String) claims.get("sub"));
            jwtClaims.setExp(claims.getExpiration().getTime());
            jwtClaims.setIat(claims.getIssuedAt().getTime());
            jwtClaims.setIss(claims.getIssuer());
            jwtClaims.setId(claims.getId());

            return Optional.of(jwtClaims);
        } catch (JwtException e) {
            return Optional.empty();
        }
    }

    public void setSecretKey(String base64URLencodedString) {

        byte[] decoded = Decoders.BASE64URL.decode(base64URLencodedString);

        this.secretKey = Keys.hmacShaKeyFor(decoded);

    }

    public void setExpireAfterMS(Long expireAfterMS) {
        this.expireAfterMS = expireAfterMS;
    }
}
