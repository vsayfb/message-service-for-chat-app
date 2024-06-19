package com.example.message_service.dto;

import lombok.Data;

@Data
public class RoomMessage {

    private RoomMessageSender sender;
    private RoomMessageAction action;

    private String targetRoomId;
    private String content;
}
