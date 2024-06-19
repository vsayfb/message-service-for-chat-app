package com.example.message_service.dto;

import lombok.Data;

@Data
public class RoomMessageDTO {

    private RoomMember sender;

    private RoomMessageAction action;

    private String targetRoomId;
}
