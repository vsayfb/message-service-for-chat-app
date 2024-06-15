package com.example.message_service.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import com.example.message_service.dto.RoomMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Controller
public class MessageMappingController {

    private final SimpMessagingTemplate messagingTemplate;

    public MessageMappingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/{roomId}")
    public RoomMessage handleMessage(
            @Payload String message,
            @DestinationVariable String roomId,
            StompHeaderAccessor accessor) {

        String username = accessor.getFirstNativeHeader("username");
        String userId = accessor.getFirstNativeHeader("userId");

        RoomMessage roomMessage = new RoomMessage();

        roomMessage.setRoomId(roomId);
        roomMessage.setUsername(username);
        roomMessage.setUserId(userId);
        roomMessage.setContent(message);

        return roomMessage;
    }
}

