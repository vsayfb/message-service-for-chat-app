package com.example.message_service.listener;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.example.message_service.dto.RoomMember;
import com.example.message_service.dto.RoomMessage;
import com.example.message_service.dto.RoomMessageAction;
import com.example.message_service.external.ExternalRoomService;
import com.example.message_service.external.dto.NewMemberResponse;
import com.example.message_service.publisher.RoomMessagePublisher;

@Component
public class WebSocketListener {

    private final RoomMessagePublisher roomMessagePublisher;

    private final ExternalRoomService externalRoomService;

    public WebSocketListener(RoomMessagePublisher roomMessagePublisher, ExternalRoomService externalRoomService) {
        this.roomMessagePublisher = roomMessagePublisher;
        this.externalRoomService = externalRoomService;
    }

    @EventListener
    void listenWebSocketDisconnection(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes.isEmpty()) {
            return;
        }

        NewMemberResponse websocketSession = (NewMemberResponse) sessionAttributes.get("user");

        try {
            externalRoomService.removeMember(websocketSession.getMemberId());

            RoomMessage roomMessage = new RoomMessage();

            roomMessage.setDestination("/topic/" + websocketSession.getRoomId());

            RoomMember roomMember = new RoomMember();

            roomMember.setMemberId(websocketSession.getMemberId());
            roomMember.setUserId(websocketSession.getUserId());
            roomMember.setUsername(websocketSession.getUsername());

            RoomMessageAction<RoomMember> messageAction = new RoomMessageAction<>();

            messageAction.setType(RoomMessageAction.Type.LEAVE);
            messageAction.setSubject(roomMember);

            roomMessage.setAction(messageAction);

            roomMessagePublisher.publish(roomMessage);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }
}
