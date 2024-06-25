package com.example.message_service.controller;

import com.example.message_service.dto.WebSocketSessionDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.stereotype.Controller;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.external.dto.NewMemberResponse;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

@Controller
public class MessageMappingController {

    @MessageMapping("/{roomId}")
    public RoomMessage handleMessage(
            @Payload String messagePayload,
            @DestinationVariable String roomId,
            StompHeaderAccessor accessor) {

        WebSocketSessionDTO websocketSession = (WebSocketSessionDTO) accessor.getSessionAttributes().get("user");

        RoomMessage roomMessage = new RoomMessage();

        roomMessage.setDestination("/topic/" + roomId);

        RoomMember roomMember = new RoomMember();

        roomMember.setUsername(websocketSession.getUsername());
        roomMember.setUserId(websocketSession.getUserId());
        roomMember.setMemberId(websocketSession.getMemberId());
        roomMember.setProfilePicture(websocketSession.getProfilePicture());

        roomMessage.setSender(roomMember);

        RoomMessageAction<String> messageAction = new RoomMessageAction<>();

        messageAction.setType(RoomMessageAction.Type.STANDARD);
        messageAction.setSubject(messagePayload);

        roomMessage.setAction(messageAction);

        return roomMessage;
    }
}
