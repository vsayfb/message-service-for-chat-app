package com.example.message_service.dto;

import lombok.Data;

@Data
public class WebSocketSessionData {

    private String memberId;
    private String username;
    private String userId;
    private String profilePicture;
    private String roomId;
    private String joinedAt;
}
