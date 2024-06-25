package com.example.message_service.jwt.claims;

import lombok.Data;

import java.util.UUID;

@Data
public class JWTClaims {

    private String sub;
    private String username;
    private String profilePicture;
    private String iss;
    private long exp;
    private long iat;
    private UUID jti;
}
