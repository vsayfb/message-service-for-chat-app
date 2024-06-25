package com.example.message_service.jwt.claims;

import lombok.Data;

@Data
public class JWTClaims {

    private String id;
    private String username;
    private String profilePicture;
    private String iss;
    private long exp;
    private long iat;
}
