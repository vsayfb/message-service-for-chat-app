package com.example.message_service.dto;

import lombok.Data;

@Data
public class RoomMessage {
    private String roomId;
    private String content;
    private String userId;
    private String username;
}
