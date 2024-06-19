package com.example.message_service.jwt;

import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.message_service.jwt.claims.JWTClaims;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTValidator {

    private SecretKey secretKey;

    public JWTValidator(@Value("${jwt_base64url_encoded_secret_key}") String base64URLencodedString) {
        setSecretKey(base64URLencodedString);
    }

    public Optional<JWTClaims> validateToken(String jwt) throws IllegalArgumentException {

        if (jwt == null || jwt.isEmpty() || !jwt.startsWith("Bearer ")) {
            return Optional.empty();
        }

        try {

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt.substring(7))
                    .getPayload();

            JWTClaims jwtClaims = new JWTClaims();

            jwtClaims.setUsername((String) claims.get("username"));
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

}
