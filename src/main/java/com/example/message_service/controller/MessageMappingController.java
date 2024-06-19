package com.example.message_service.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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

        NewMemberResponse websocketSession = (NewMemberResponse) accessor.getSessionAttributes().get("user");

        RoomMessage roomMessage = new RoomMessage();

        roomMessage.setDestination("/topics/" + roomId);

        RoomMember roomMember = new RoomMember();

        roomMember.setUsername(websocketSession.getUsername());
        roomMember.setUserId(websocketSession.getUserId());
        roomMember.setMemberId(websocketSession.getMemberId());

        roomMessage.setSender(roomMember);

        RoomMessageAction<String> messageAction = new RoomMessageAction<>();

        messageAction.setType(RoomMessageAction.Type.STANDARD);
        messageAction.setSubject(messagePayload);

        return roomMessage;
    }
}
