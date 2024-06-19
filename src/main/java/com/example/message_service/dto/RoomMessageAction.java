package com.example.message_service.dto;

import lombok.Data;

@Data
public class RoomMessageAction<T> {

    public enum Type {
        JOIN,
        LEAVE,
        STANDARD
    }

    private Type type;

    private T subject;
}

