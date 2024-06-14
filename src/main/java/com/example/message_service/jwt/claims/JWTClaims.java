package com.example.message_service.jwt.claims;

import lombok.Data;

@Data
public class JWTClaims {

    private String username;
    private String userId;
    private String roomId;
    private String iss;
    private long exp;
    private long iat;
    private String id;
}
