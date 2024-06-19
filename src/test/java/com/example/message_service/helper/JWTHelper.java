package com.example.message_service.helper;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;

@Component
@Data
public class JWTHelper {

    private SecretKey secretKey;

    private String username;
    private String userId;

    public JWTHelper(@Value("${jwt_base64url_encoded_secret_key}") String base64UrlEncodedString) {
        setSecretKey(base64UrlEncodedString);
    }

    public String sign() {

        return Jwts.builder()
                .signWith(secretKey)
                .claim("sub", userId)
                .claim("username", username)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .issuedAt(new Date())
                .issuer("test")
                .id(UUID.randomUUID().toString())
                .compact();

    }

    private void setSecretKey(String base64UrlEncode) {

        byte[] decoded = Decoders.BASE64URL.decode(base64UrlEncode);

        this.secretKey = Keys.hmacShaKeyFor(decoded);
    }
}
