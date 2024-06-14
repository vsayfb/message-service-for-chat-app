package com.example.message_service.jwt.claims;

import lombok.Data;

@Data
public class JWTPayload {
    private String userId;
    private String username;
    private String roomId;
}
