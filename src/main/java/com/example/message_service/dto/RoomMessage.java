package com.example.message_service.dto;

import lombok.Data;

@Data
public class RoomMessage {

    private RoomMember sender;

    private RoomMessageAction action;

    private String destination;
}
