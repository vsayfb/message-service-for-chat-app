package com.example.message_service.controller;

import com.example.message_service.dto.WebSocketSessionDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.stereotype.Controller;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.websocket.manager.WebSocketSessionManager;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Controller
public class MessageMappingController {

    private final WebSocketSessionManager webSocketSessionManager;

    public MessageMappingController(WebSocketSessionManager webSocketSessionManager) {
        this.webSocketSessionManager = webSocketSessionManager;
    }

    @MessageMapping("/{roomId}")
    public RoomMessage handleMessage(
            @Payload String messagePayload,
            @DestinationVariable String roomId,
            StompHeaderAccessor accessor) {

        WebSocketSessionDTO authenticatedUser = webSocketSessionManager.getAuthenticatedUser(accessor);

        RoomMessage roomMessage = new RoomMessage();

        roomMessage.setDestination("/topic/" + roomId);

        RoomMember roomMember = new RoomMember();

        roomMember.setUsername(authenticatedUser.getUsername());
        roomMember.setUserId(authenticatedUser.getUserId());
        roomMember.setMemberId(authenticatedUser.getMemberId());
        roomMember.setProfilePicture(authenticatedUser.getProfilePicture());

        roomMessage.setSender(roomMember);

        RoomMessageAction<String> messageAction = new RoomMessageAction<>();

        messageAction.setType(RoomMessageAction.Type.STANDARD);
        messageAction.setSubject(messagePayload);

        roomMessage.setAction(messageAction);

        return roomMessage;
    }
}
